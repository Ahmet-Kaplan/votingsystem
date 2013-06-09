package org.sistemavotacion.controlacceso

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.sistemavotacion.seguridad.*;
import org.sistemavotacion.smime.SMIMEMessageWrapper;
import org.sistemavotacion.util.*;
import org.sistemavotacion.controlacceso.modelo.*;

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.*
import java.io.File;
import java.io.FileOutputStream;
import javax.mail.internet.MimeMessage;
import java.util.Locale;

/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
*/
class EventoReclamacionService {	
		
    static transactional = true

    def etiquetaService
    def firmaService
    def eventoService
    def grailsApplication
	def messageSource
	def filesService
	

    Respuesta saveEvent(MensajeSMIME mensajeSMIMEReq, Locale locale) {		
		EventoReclamacion evento
		try {
			Usuario firmante = mensajeSMIMEReq.getUsuario()
			String documentStr = mensajeSMIMEReq.getSmimeMessage().getSignedContent()
			log.debug("saveEvent - firmante: ${firmante.nif} - documentStr: ${documentStr}")
			def mensajeJSON = JSON.parse(documentStr)
			evento = new EventoReclamacion(usuario:firmante,
					asunto:mensajeJSON.asunto, contenido:mensajeJSON.contenido,
					copiaSeguridadDisponible:mensajeJSON.copiaSeguridadDisponible,
					fechaFin: new Date().parse(
						"yyyy-MM-dd HH:mm:ss", mensajeJSON.fechaFin))
			if(mensajeJSON.cardinalidad) evento.cardinalidadRepresentaciones =
				Evento.Cardinalidad.valueOf(mensajeJSON.cardinalidad)
			else evento.cardinalidadRepresentaciones = Evento.Cardinalidad.UNA
			if(mensajeJSON.fechaInicio) evento.fechaInicio = new Date().parse(
						"yyyy-MM-dd HH:mm:ss", mensajeJSON.fechaInicio)
			else evento.fechaInicio = DateUtils.getTodayDate();
			Respuesta respuesta = eventoService.setEventDatesState(evento, locale)
			if(Respuesta.SC_OK != respuesta.codigoEstado) return respuesta 
			evento = respuesta.evento.save()
			if (mensajeJSON.etiquetas) {
				Set<Etiqueta> etiquetaSet = etiquetaService.guardarEtiquetas(mensajeJSON.etiquetas)
				evento.setEtiquetaSet(etiquetaSet)
			}
			mensajeJSON.id = evento.id
			mensajeJSON.fechaCreacion = DateUtils.getStringFromDate(evento.dateCreated)
			mensajeJSON.tipo = Tipo.EVENTO_RECLAMACION
			def camposValidados = []
			JSONArray arrayCampos = new JSONArray()
			mensajeJSON.campos?.collect { campoItem ->
				def campo = new CampoDeEvento(evento:evento, contenido:campoItem.contenido)
				campo.save();
				arrayCampos.add(new JSONObject([id:campo.id, contenido:campo.contenido]))
			}
			mensajeJSON.controlAcceso = [serverURL:grailsApplication.config.grails.serverURL,
				nombre:grailsApplication.config.SistemaVotacion.serverName]  as JSONObject
			mensajeJSON.campos = arrayCampos
			
			String fromUser = grailsApplication.config.SistemaVotacion.serverName
			String toUser = firmante.getNif()
			String subject = messageSource.getMessage(
					'mime.asunto.EventoReclamacionValidado', null, locale)
			
			byte[] smimeMessageRespBytes = firmaService.getSignedMimeMessage(
				fromUser, toUser,  mensajeJSON.toString(), subject, null)
			
			MensajeSMIME mensajeSMIMEResp = new MensajeSMIME(tipo:Tipo.RECIBO,
				smimePadre:mensajeSMIMEReq, evento:evento,
				valido:true, contenido:smimeMessageRespBytes)
			MensajeSMIME.withTransaction {
				mensajeSMIMEResp.save()
			}
			return new Respuesta(codigoEstado:Respuesta.SC_OK, evento:evento,
				mensajeSMIME:mensajeSMIMEResp, tipo:Tipo.EVENTO_RECLAMACION)
		} catch(Exception ex) {
			log.error (ex.getMessage(), ex)
			return new Respuesta(codigoEstado:Respuesta.SC_ERROR,
				mensaje:messageSource.getMessage('publishClaimErrorMessage', null, locale), 
				tipo:Tipo.EVENTO_RECLAMACION_ERROR, evento:evento)
		}
    }

    public synchronized Respuesta generarCopiaRespaldo (EventoReclamacion evento, Locale locale) {
        log.debug("generarCopiaRespaldo - eventoId: ${evento.id}")
		Respuesta respuesta;
        if (!evento) {
			return new Respuesta(codigoEstado:Respuesta.SC_ERROR_PETICION, mensaje:
				messageSource.getMessage('evento.peticionSinEvento', null, locale))
        }
		
		Map<String, File> mapFiles = filesService.getBackupFiles(
			evento, Tipo.EVENTO_RECLAMACION, locale)
		File metaInfFile = mapFiles.metaInfFile
		File filesDir = mapFiles.filesDir
		File zipResult   = mapFiles.zipResult
		

		
		def firmasRecibidas = Firma.findAllWhere(evento:evento)		
		def metaInfMap = [numSignatures:firmasRecibidas.size()]
		Evento.withTransaction {
			evento.updateMetaInf(Tipo.BACKUP, metaInfMap)
		}
		metaInfFile.write(evento.metaInf)
		
		String fileNamePrefix = messageSource.getMessage('claimLbl', null, locale);
		int i = 0
		
		firmasRecibidas.each { firma ->
			MensajeSMIME mensajeSMIME = firma.mensajeSMIME
			File smimeFile = new File("${filesDir.absolutePath}/${fileNamePrefix}_${String.format('%08d', i++)}.p7m")
			smimeFile.setBytes(mensajeSMIME.contenido)
		}
		
		def ant = new AntBuilder()
		ant.zip(destfile: zipResult, basedir: "${filesDir.absolutePath}") {
			fileset(dir:"${filesDir}/..", includes: "meta.inf")
		}

		return new Respuesta(codigoEstado:Respuesta.SC_OK,
			tipo:Tipo.EVENTO_RECLAMACION, file:zipResult)
    }

}