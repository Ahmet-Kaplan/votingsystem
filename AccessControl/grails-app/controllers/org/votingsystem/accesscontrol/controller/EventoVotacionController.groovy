package org.votingsystem.accesscontrol.controller

import java.util.HashSet;
import java.util.Set;

import org.votingsystem.accesscontrol.model.*

import grails.converters.JSON

import java.security.cert.X509Certificate;

import org.votingsystem.model.ResponseVS;
import org.votingsystem.model.TypeVS;
import org.votingsystem.signature.util.CertUtil;
import org.votingsystem.model.ContextVS
import javax.mail.Header;

import org.votingsystem.util.*

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
/**
 * @infoController Votaciones
 * @descController Servicios relacionados con la publicación de votaciones.
 * 
 * @author jgzornoza
 * Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
 */
class EventoVotacionController {

    def eventoVotacionService
    def eventoService

	
	
	/**
	 * @httpMethod [GET]
	 * @return La página principal de la aplicación web de votación.
	 */
	def mainPage() {
		render(view:"mainPage" , model:[selectedSubsystem:Subsystem.VOTES.toString()])
	}
	
	/**
	 * @httpMethod [GET]
     * @serviceURL [/eventoVotacion/$id]	 
	 * @param [id] Opcional. El identificador de la votación en la base de datos. Si no se pasa ningún id
	 *        la consulta se hará entre todos las votaciones.
	 * @param [max] Opcional (por defecto 20). Número máximo de votaciones que
	 * 		  devuelve la consulta (tamaño de la página).
	 * @param [offset] Opcional (por defecto 0). Indice a partir del cual se pagina el resultado.
	 * @param [order] Opcional, posibles valores 'asc', 'desc'(por defecto). Orden en que se muestran los
	 *        resultados según la fecha de inicio.
	 * @responseContentType [application/json]
	 * @return Documento JSON con las votaciones que cumplen con el criterio de búsqueda.
	 */
	def index() {
		try {
			def eventoList = []
			def eventosMap = new HashMap()
			eventosMap.eventos = new HashMap()
			eventosMap.eventos.votaciones = []
			if (params.long('id')) {
				EventoVotacion evento = null
				EventoVotacion.withTransaction {
					evento = EventoVotacion.get(params.long('id'))
				}
				if(!(evento.estado == Evento.Estado.ACTIVO || evento.estado == Evento.Estado.PENDIENTE_COMIENZO ||
					evento.estado == Evento.Estado.CANCELADO || evento.estado == Evento.Estado.FINALIZADO)) evento = null
				if(!evento) {
					response.status = ResponseVS.SC_NOT_FOUND
					render message(code: 'eventNotFound', args:[params.id])
					return false
				} else {
					if(request.contentType?.contains("application/json")) {
						render eventoService.optenerEventoMap(evento) as JSON
						return false
					} else {
						render(view:"eventoVotacion", model: [
							selectedSubsystem:Subsystem.VOTES.toString(),
							eventMap: eventoService.optenerEventoMap(evento)])
						return
					}
				}
			} else {
				params.sort = "fechaInicio"
				log.debug " -Params: " + params
				Evento.Estado estadoEvento
				if(params.estadoEvento) estadoEvento = Evento.Estado.valueOf(params.estadoEvento)
				EventoVotacion.withTransaction {
					if(estadoEvento) {
						if(estadoEvento == Evento.Estado.FINALIZADO) {
							eventoList =  EventoVotacion.findAllByEstadoOrEstado(
								Evento.Estado.CANCELADO, Evento.Estado.FINALIZADO, params)
							eventosMap.numeroTotalEventosVotacionEnSistema = EventoVotacion.countByEstadoOrEstado(
								Evento.Estado.CANCELADO, Evento.Estado.FINALIZADO)
						} else {
							eventoList =  EventoVotacion.findAllByEstado(estadoEvento, params)
							log.debug " -estadoEvento: " + estadoEvento
							eventosMap.numeroTotalEventosVotacionEnSistema = EventoVotacion.countByEstado(estadoEvento)
						}
					} else {
						eventoList =  EventoVotacion.findAllByEstadoOrEstadoOrEstadoOrEstado(Evento.Estado.ACTIVO,
							   Evento.Estado.CANCELADO, Evento.Estado.FINALIZADO, Evento.Estado.PENDIENTE_COMIENZO, params)
						eventosMap.numeroTotalEventosVotacionEnSistema =
								EventoVotacion.countByEstadoOrEstadoOrEstadoOrEstado(Evento.Estado.ACTIVO,
								Evento.Estado.CANCELADO, Evento.Estado.FINALIZADO, Evento.Estado.PENDIENTE_COMIENZO)
					}
				}
				eventosMap.offset = params.long('offset')
			}
			eventosMap.numeroEventosVotacionEnPeticion = eventoList.size()
			eventoList.each {eventoItem ->
					eventosMap.eventos.votaciones.add(eventoService.optenerEventoVotacionMap(eventoItem))
			}
			response.setContentType("application/json")
			render eventosMap as JSON
		} catch(Exception ex) {
			log.error (ex.getMessage(), ex)
			response.status = ResponseVS.SC_ERROR_PETICION
			render ex.getMessage()
			return false
		}
	}
	
	/**
	 * Servicio para publicar votaciones.
	 *
	 * @serviceURL [/eventoVotacion]
	 * @httpMethod [POST]
	 * 
	 * @requestContentType [application/x-pkcs7-signature, application/x-pkcs7-mime] Obligatorio. Documento firmado
	 *                     en formato SMIME con los datos de la votación que se desea publicar
	 * @responseContentType [application/x-pkcs7-signature] Obligatorio. Recibo firmado por el sistema.
	 * 
	 * @return Recibo que consiste en el archivo firmado recibido con la firma añadida del servidor.
	 */
    def save () {
		MessageSMIME messageSMIME = params.messageSMIMEReq
		if(!messageSMIME) {
			String msg = message(code:'evento.peticionSinArchivo')
			log.error msg
			response.status = ResponseVS.SC_ERROR_PETICION
			render msg
			return false
		}
        ResponseVS respuesta = eventoVotacionService.saveEvent(
			messageSMIME, request.getLocale())
		params.respuesta = respuesta
		if (ResponseVS.SC_OK == respuesta.statusCode) {
			response.setHeader('eventURL', 
				"${grailsApplication.config.grails.serverURL}/eventoVotacion/${respuesta.eventVS.id}")
			response.contentType =  ContextVS.SIGNED_CONTENT_TYPE
		}
    }

	/**
	 * Servicio que devuelve estadísticas asociadas a una votación.
	 *
	 * @httpMethod [GET]
	 * @serviceURL [/eventoVotacion/$id/estadisticas]
	 * @param [id] Obligatorio. Identificador en la base de datos de la votación que se desea consultar.
         * @requestContentType [application/json] Para solicitar una respuesta en formato JSON
         * @requestContentType [text/html] Para solicitar una respuesta en formato HTML
         * @responseContentType [application/json]                    
	 * @responseContentType [text/html]
	 * @return Documento JSON con las estadísticas asociadas a la votación solicitada.
	 */
    def estadisticas () {
        if (params.long('id')) {
            EventoVotacion eventoVotacion
            if (!params.evento) {
				EventoVotacion.withTransaction {
					eventoVotacion = EventoVotacion.get(params.id)
				}
			} 
            else eventoVotacion = params.evento
            if (eventoVotacion) {
                response.status = ResponseVS.SC_OK
                def statisticsMap = eventoVotacionService.getStatisticsMap(
			eventoVotacion, request.getLocale())
                if(request.contentType?.contains("application/json")) {
                    if (params.callback) render "${params.callback}(${statisticsMap as JSON})"
                    else render statisticsMap as JSON
                    return false
                } else {
                    render(view:"statistics", model: [statisticsJSON: statisticsMap  as JSON])
                    return
                }
            }
            response.status = ResponseVS.SC_NOT_FOUND
            render message(code: 'eventNotFound', args:[params.id])
            return false
        }
        response.status = ResponseVS.SC_ERROR_PETICION
        render message(code: 'error.PeticionIncorrectaHTML', args:[
			"${grailsApplication.config.grails.serverURL}/${params.controller}"])
        return false
    }
    
	/**
	 * Servicio que proporciona una copia de la votación publicada con la firma
	 * añadida del servidor.
	 *
	 * @httpMethod [GET]
	 * @serviceURL [/eventoVotacion/${id}/validado]
	 * @param [id] Obligatorio. El identificador de la votación en la base de datos.
	 * @return Archivo SMIME de la publicación de la votación firmada por el usuario que
	 *         la publicó y el servidor.
	 */
    def validado () {
        if (params.long('id')) {
			def evento
			Evento.withTransaction {
				evento = Evento.get(params.id)
			}
            MessageSMIME messageSMIME
            if (evento) {
				MessageSMIME.withTransaction {
					List results = MessageSMIME.withCriteria {
						createAlias("smimePadre", "smimePadre")
						eq("smimePadre.evento", evento)
						eq("smimePadre.type", TypeVS.EVENTO_VOTACION)
					}
					messageSMIME = results.iterator().next()
				}
				
				log.debug("messageSMIME.id: ${messageSMIME.id}")
                if (messageSMIME) {
                        response.status = ResponseVS.SC_OK
                        response.contentLength = messageSMIME.contenido.length
                        //response.setContentType("text/plain")
                        response.outputStream <<  messageSMIME.contenido
                        response.outputStream.flush()
                        return false
                }
            }
            if (!evento || !messageSMIME) {
                response.status = ResponseVS.SC_NOT_FOUND
                render message(code: 'eventNotFound', args:[params.id])
                return false
            }
        }
        response.status = ResponseVS.SC_ERROR_PETICION
        render message(code: 'error.PeticionIncorrectaHTML', args:[
			"${grailsApplication.config.grails.serverURL}/${params.controller}/restDoc"])
        return false
    }
	
	/**
	 * Servicio que proporciona una copia de la votación publicada.
	 *
	 * @httpMethod [GET]
	 * @serviceURL [/eventoVotacion/${id}/firmado]
	 * @param [id] Obligatorio. El identificador de la votación en la base de datos.
	 * @return Archivo SMIME de la publicación de la votación firmada por el usuario
	 *         que la publicó.
	 */
	def firmado () {
		if (params.long('id')) {
			def evento
			Evento.withTransaction {
				evento = Evento.get(params.id)
			}
			if (evento) {
				MessageSMIME messageSMIME
				MessageSMIME.withTransaction {
					messageSMIME = MessageSMIME.findWhere(evento:evento, type: TypeVS.EVENTO_VOTACION)
				}
				if (messageSMIME) {
					response.status = ResponseVS.SC_OK
					response.contentLength = messageSMIME.contenido.length
					response.setContentType("text/plain")
					response.outputStream <<  messageSMIME.contenido
					response.outputStream.flush()
					return false
				}
			}
			response.status = ResponseVS.SC_NOT_FOUND
			render message(code: 'eventNotFound', args:[params.id])
			return false
		}
		response.status = ResponseVS.SC_ERROR_PETICION
		render message(code: 'error.PeticionIncorrectaHTML', args:[
			"${grailsApplication.config.grails.serverURL}/${params.controller}"])
		return false
	}

	/**
	 * Servicio que devuelve información sobre la actividad de una votación
	 *
	 * @httpMethod [GET]
	 * @serviceURL [/eventoVotacion/$id/informacionVotos]
	 * @param [id] Obligatorio. El identificador de la votación en la base de datos.
	 * @responseContentType [application/json]
	 * @return Documento JSON con información sobre los votos y solicitudes de acceso de una votación.
	 */
	def informacionVotos () {
		if (params.long('id')) {
			EventoVotacion evento
			EventoVotacion.withTransaction {
				evento = EventoVotacion.get(params.long('id'))
			}
			if (!evento) {
				response.status = ResponseVS.SC_NOT_FOUND
				render message(code: 'eventNotFound', args:[params.id])
				return false
			}
			def informacionVotosMap = new HashMap()
			def solicitudesOk, solicitudesAnuladas;
			SolicitudAcceso.withTransaction {
				solicitudesOk = SolicitudAcceso.findAllWhere(
					estado:TypeVS.OK, eventoVotacion:evento)
				solicitudesAnuladas = SolicitudAcceso.findAllWhere(
					estado:TypeVS.ANULADO, eventoVotacion:evento)
			}
			def solicitudesCSROk, solicitudesCSRAnuladas;
			SolicitudCSRVoto.withTransaction {
				solicitudesCSROk = SolicitudCSRVoto.findAllWhere(
					estado:SolicitudCSRVoto.Estado.OK, eventoVotacion:evento)
				solicitudesCSRAnuladas = SolicitudCSRVoto.findAllWhere(
					estado:SolicitudCSRVoto.Estado.ANULADA, eventoVotacion:evento)
			}
			informacionVotosMap.numeroTotalSolicitudes = evento.solicitudesAcceso.size()
			informacionVotosMap.numeroSolicitudes_OK = solicitudesOk.size()
			informacionVotosMap.numeroSolicitudes_ANULADAS = solicitudesAnuladas.size()
			informacionVotosMap.numeroTotalCSRs = evento.solicitudesCSR.size()
			informacionVotosMap.numeroSolicitudesCSR_OK = solicitudesCSROk.size()
			informacionVotosMap.solicitudesCSRAnuladas = solicitudesCSRAnuladas.size()
			informacionVotosMap.certificadoRaizEvento = "${grailsApplication.config.grails.serverURL}" +
				"/certificado/eventCA/${params.id}"
			informacionVotosMap.solicitudesAcceso = []
			informacionVotosMap.votos = []
			informacionVotosMap.opciones = []
			evento.solicitudesAcceso.each { solicitud ->
				def solicitudMap = [id:solicitud.id, fechaCreacion:solicitud.dateCreated,
				estado:solicitud.estado.toString(),
				hashSolicitudAccesoBase64:solicitud.hashSolicitudAccesoBase64,
				usuario:solicitud.usuario.nif,
				solicitudAccesoURL:"${grailsApplication.config.grails.serverURL}/messageSMIME" +
					"/obtener?id=${solicitud?.messageSMIME?.id}"]
				if(SolicitudAcceso.Estado.ANULADO.equals(solicitud.estado)) {
					AnuladorVoto anuladorVoto = AnuladorVoto.findWhere(solicitudAcceso:solicitud)
					solicitudMap.anuladorURL="${grailsApplication.config.grails.serverURL}" +
						"/messageSMIME/${anuladorVoto?.messageSMIME?.id}"
				}
				informacionVotosMap.solicitudesAcceso.add(solicitudMap)
			}
			evento.opciones.each { opcion ->
				def numeroVotos = Voto.findAllWhere(opcionDeEvento:opcion, estado:Voto.Estado.OK).size()
				def opcionMap = [id:opcion.id, contenido:opcion.contenido,
					numeroVotos:numeroVotos]
				informacionVotosMap.opciones.add(opcionMap)
			}
			
			HexBinaryAdapter hexConverter = new HexBinaryAdapter();
			evento.votos.each { voto ->
				def hashCertificadoVotoHex = hexConverter.marshal(
					voto.certificado.hashCertificadoVotoBase64.getBytes() )
				def votoMap = [id:voto.id, opcionSeleccionadaId:voto.opcionDeEvento.id,
					estado:voto.estado.toString(),
					hashCertificadoVotoBase64:voto.certificado.hashCertificadoVotoBase64,
					certificadoURL:"${grailsApplication.config.grails.serverURL}/certificado" +
						"/voto/${hashCertificadoVotoHex}",
					votoURL:"${grailsApplication.config.grails.serverURL}/messageSMIME" +
						"/${voto.messageSMIME.id}"]
				if(Voto.Estado.ANULADO.equals(voto.estado)) {
					AnuladorVoto anuladorVoto = AnuladorVoto.findWhere(voto:voto)
					votoMap.anuladorURL="${grailsApplication.config.grails.serverURL}" +
						"/messageSMIME/${anuladorVoto?.messageSMIME?.id}"
				}
				informacionVotosMap.votos.add(votoMap)
			}
			response.status = ResponseVS.SC_OK
			response.setContentType("application/json")
			render informacionVotosMap as JSON
		}
		response.status = ResponseVS.SC_ERROR_PETICION
		render message(code: 'error.PeticionIncorrectaHTML',
			args:["${grailsApplication.config.grails.serverURL}/${params.controller}/restDoc"])
		return false
	}

	/**
	 * Servicio que devuelve un archivo zip con los errores que se han producido
	 * en una votación
	 *
	 * @httpMethod [GET]
	 * @serviceURL [/eventoVotacion/$id/votingErrors]
	 * @param [id] Obligatorio. Identificador del evento en la base de datos
	 * @return Archivo zip con los messages con errores
	 */
	def votingErrors() {
		EventoVotacion event
		EventoVotacion.withTransaction {
			event = EventoVotacion.get(params.long('id'))
		}
		if (!event) {
			response.status = ResponseVS.SC_NOT_FOUND
			render message(code: 'eventNotFound', args:[params.id])
			return false
		}
		def errors
		MessageSMIME.withTransaction {
			errors = MessageSMIME.findAllWhere (
				type:TypeVS.VOTO_CON_ERRORES,  evento:event)
		}
		
		if(errors.size == 0){
			response.status = ResponseVS.SC_OK
			render message(code: 'votingWithoutErrorsMsg',
				args:[event.id, event.asunto])
		} else {
			String datePathPart = DateUtils.getShortStringFromDate(event.getDateFinish())
			String baseDirPath = "${grailsApplication.config.VotingSystem.errorsBaseDir}" +
				"/${datePathPart}/Event_${event.id}"
			errors.each { messageSMIME ->
				File errorFile = new File("${baseDirPath}/MessageSMIME_${messageSMIME.id}")
				errorFile.setBytes(messageSMIME.contenido)
			}
			File zipResult = new File("${baseDirPath}.zip")
			def ant = new AntBuilder()
			ant.zip(destfile: zipResult, basedir: "${baseDirPath}")
			
			response.setContentType("application/zip")
		}
	}
	
	
}