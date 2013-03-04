package org.sistemavotacion.controlacceso

import javax.xml.bind.annotation.adapters.HexBinaryAdapter
import org.sistemavotacion.controlacceso.modelo.*;
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.MultipartFile;
import grails.converters.JSON

/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/blob/master/licencia.txt
*/
class SolicitudAccesoController {
    
    def solicitudAccesoService
	def firmaService
	def csrService

	def index = {}
	
    def obtener = {
        if (params.long('id')) {
			def solicitudAcceso
			SolicitudAcceso.withTransaction {
				solicitudAcceso = SolicitudAcceso.get(params.id)
			}
            if (solicitudAcceso) {
                    response.status = Respuesta.SC_OK
                    response.contentLength = solicitudAcceso.mensajeSMIME?.contenido?.length
                    response.setContentType("text/plain")
                    response.outputStream <<  solicitudAcceso.mensajeSMIME?.contenido
                    response.outputStream.flush()
                    return false
            }
            response.status = Respuesta.SC_NOT_FOUND
            render  message(code: 'anulacionVoto.errorSolicitudNoEncontrada')
            return false
        }
        response.status = Respuesta.SC_ERROR_PETICION
        render message(code: 'error.PeticionIncorrecta')
        return false
    }

    def procesar = { 
        String nombreEntidadFirmada = grailsApplication.config.SistemaVotacion.nombreEntidadFirmada;
		SolicitudAcceso solicitudAcceso;
		Respuesta respuesta;
        if (request instanceof MultipartHttpServletRequest) {
			try {
				Map multipartFileMap = ((MultipartHttpServletRequest) request)?.getFileMap()
				MultipartFile solicitudAccesoMultipartFile = multipartFileMap.remove(nombreEntidadFirmada)                                
				respuesta = solicitudAccesoService.validarSolicitud(
					solicitudAccesoMultipartFile.getBytes(), request.getLocale())
				solicitudAcceso = respuesta.solicitudAcceso
				if (Respuesta.SC_OK == respuesta.codigoEstado) {
					MultipartFile solicitudCsrFile = multipartFileMap.remove(
						grailsApplication.config.SistemaVotacion.nombreSolicitudCSR)
					Respuesta respuestaValidacionCSR = firmaService.firmarCertificadoVoto(solicitudCsrFile.getBytes(), 
						respuesta.evento, request.getLocale())
					respuesta = respuestaValidacionCSR
					if (Respuesta.SC_OK == respuestaValidacionCSR.codigoEstado) {
						response.contentLength = respuestaValidacionCSR.firmaCSR.length
						response.setContentType("application/octet-stream")
						response.outputStream << respuestaValidacionCSR.firmaCSR
						response.outputStream.flush()
						return false
					} else {
						if (solicitudAcceso)
							solicitudAccesoService.rechazarSolicitud(solicitudAcceso)
					}
				} else {
					response.status = respuesta.codigoEstado
					render respuesta?.mensaje
					return false;
				}
			} catch (Exception ex) {
				log.error (ex.getMessage(), ex)
				if (solicitudAcceso)
					solicitudAccesoService.rechazarSolicitud(solicitudAcceso)
				String mensaje = ex.getMessage();
				if(!mensaje || "".equals(mensaje)) {
					mensaje = message(code: 'error.PeticionIncorrecta')
				}
				response.status = Respuesta.SC_ERROR_EJECUCION
				render mensaje
				return false;
			}
        }	
		response.status = Respuesta.SC_ERROR_PETICION
		render message(code: 'error.PeticionIncorrecta')
		return false
    }
    
    def encontrar = {
        if (params.hashSolicitudAccesoHex) {
            HexBinaryAdapter hexConverter = new HexBinaryAdapter();
            String hashSolicitudAccesoBase64 = new String(
				hexConverter.unmarshal(params.hashSolicitudAccesoHex))
            log.debug "hashSolicitudAccesoBase64: ${hashSolicitudAccesoBase64}"
            SolicitudAcceso solicitudAcceso = SolicitudAcceso.findWhere(hashSolicitudAccesoBase64:
                hashSolicitudAccesoBase64)
            if (solicitudAcceso) {
                response.status = Respuesta.SC_OK
                response.contentLength = solicitudAcceso.contenido.length
                response.setContentType("text/plain")
                response.outputStream <<  solicitudAcceso.contenido
                response.outputStream.flush()
                return false  
            }
            response.status = Respuesta.SC_NOT_FOUND
            render message(code: 'error.solicitudAccesoNotFound', 
                args:[params.hashSolicitudAccesoHex])
            return false
        }
        response.status = Respuesta.SC_ERROR_PETICION
        render message(code: 'error.PeticionIncorrectaHTML', args:["${grailsApplication.config.grails.serverURL}/${params.controller}"])
        return false
    }
	
	def encontrarPorNif = {
		if(params.nif && params.long('eventoId')) {
			EventoVotacion evento
			EventoVotacion.withTransaction {
				evento =  EventoVotacion.get(params.eventoId)
			}
			if(!evento) {
				response.status = Respuesta.SC_NOT_FOUND
				render message(code: 'evento.eventoNotFound', args:[params.eventoId])
				return
			}
			Usuario usuario
			Usuario.withTransaction {
				usuario =  Usuario.findByNif(params.nif)
			}
			if(!usuario) {
				response.status = Respuesta.SC_NOT_FOUND
				render message(code: 'usuario.nifNoEncontrado', args:[params.nif])
				return
			}
			SolicitudAcceso solicitudAcceso
			SolicitudAcceso.withTransaction {
				solicitudAcceso =  SolicitudAcceso.findWhere(
					usuario: usuario, eventoVotacion:evento)
			}
			if(!solicitudAcceso) {
				response.status = Respuesta.SC_NOT_FOUND
				render message(code: 'error.nifSinSolicitudAcceso', args:[params.eventoId, params.nif])
				return
			}
			response.status = Respuesta.SC_OK
			response.contentLength = solicitudAcceso.mensajeSMIME?.contenido.length
			response.setContentType("text/plain")
			response.outputStream <<  solicitudAcceso.mensajeSMIME?.contenido
			response.outputStream.flush()
			return false
		}
		response.status = Respuesta.SC_ERROR_PETICION
		render message(code: 'error.PeticionIncorrectaHTML', args:["${grailsApplication.config.grails.serverURL}/${params.controller}"])
		return false
	}

	
}