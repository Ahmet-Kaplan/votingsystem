package org.votingsystem.controlcenter.controller

import org.votingsystem.model.ContentTypeVS
import org.votingsystem.model.EventVSElection
import org.votingsystem.model.MessageSMIME
import org.votingsystem.model.ResponseVS
import org.votingsystem.model.SubSystemVS;
import org.votingsystem.model.TypeVS
import grails.converters.JSON
import org.votingsystem.model.VoteVS
import org.votingsystem.model.VoteVSCanceller
import org.votingsystem.util.DateUtils
import org.votingsystem.model.EventVS
import javax.xml.bind.annotation.adapters.HexBinaryAdapter
/**
 * @infoController Votaciones
 * @descController Servicios relacionados con las votaciones publicadas en el servidor.
 * 
 * @author jgzornoza
 * Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
 * */
class EventVSElectionController {

    def eventVSElectionService
	def subscriptionVSService
	def grailsApplication
	
	/**
	 * @httpMethod [GET]
	 * @return La página principal de la aplicación web de votación.
	 */
	def main() {
		render(view:"main" , model:[selectedSubsystem:SubSystemVS.VOTES.toString()])
	}

	/**
	 * Servicio de consulta de las votaciones publicadas.
	 *
	 * @param [id]  Opcional. El identificador en la base de datos del documento que se
	 * 			  desee consultar.
	 * @param [max]	Opcional (por defecto 20). Número máximo de documentos que
	 * 		  devuelve la consulta (tamaño de la página).
	 * @param [offset]	Opcional (por defecto 0). Indice a partir del cual se pagina el resultado.
	 * @httpMethod [GET]
	 * @serviceURL [/eventVS/$id?]
	 * @responseContentType [application/json]
	 * @return Documento JSON con las votaciones que cumplen el criterio de búsqueda.
	 */
	def index() {
		List eventVSList = []
		def responseMap = new HashMap()
		responseMap.eventsVS = new HashMap()
		responseMap.eventsVS.elections = []
		if (params.long('id')) {
            EventVSElection eventVS = null
            EventVSElection.withTransaction { eventVS = EventVSElection.get(params.long('id')) }
			if(!eventVS) {
                return [responseVS : new ResponseVS(ResponseVS.SC_NOT_FOUND,
                        message(code: 'eventVSNotFoundErrorMsg', args:[params.id]))]
			} else {
				Map eventMap = eventVSElectionService.getEventVSElectionMap(eventVS)
				if(request.contentType?.contains(ContentTypeVS.JSON.getName())) {
					render eventMap as JSON
					return
				} else {
					render(view:"eventVSElection", model: [selectedSubsystem:SubSystemVS.VOTES.toString(),
                            eventMap: eventMap])
					return
				}
			}
		} else {
			params.sort = "dateBegin"
			EventVS.State eventVSState
			if(params.eventVSState) eventVSState = EventVS.State.valueOf(params.eventVSState)
            EventVSElection.withTransaction {
				if(eventVSState) {
					if(eventVSState == EventVS.State.TERMINATED) {
						eventVSList =  EventVSElection.findAllByStateOrState(
							EventVS.State.CANCELLED, EventVS.State.TERMINATED, params)
					} else {
						eventVSList =  EventVS.findAllByState(eventVSState, params)
					}
				} else {
					eventVSList =  EventVSElection.findAllByStateOrStateOrStateOrState(EventVS.State.ACTIVE,
						   EventVS.State.CANCELLED, EventVS.State.TERMINATED, EventVS.State.AWAITING, params)
				}
			}
			responseMap.offset = params.long('offset')
		}
		responseMap.numEventsVSElectionInSystem = EventVSElection.countByStateOrStateOrStateOrState(
			EventVS.State.ACTIVE, EventVS.State.CANCELLED,
			EventVS.State.TERMINATED, EventVS.State.AWAITING)
		responseMap.numEventsVSElection = eventVSList.size()
		eventVSList.each {eventVSItem ->
				responseMap.eventsVS.elections.add(eventVSElectionService.getEventVSElectionMap(eventVSItem))
		}
		render responseMap as JSON
	}
	
	
	/**
	 * Servicio que da de alta las votaciones.
	 * 
	 * @httpMethod [POST]
	 * @serviceURL [/eventVS]
	 * @contentType [application/x-pkcs7-signature] Obligatorio. El archivo con los datos de la votación firmado
	 * 		  por el usuario que la publica y el Control de Acceso en el que se publica.
	 */
	def save () {
		MessageSMIME messageSMIME = request.messageSMIMEReq
		if(!messageSMIME) {
            return [responseVS : new ResponseVS(ResponseVS.SC_ERROR_REQUEST,message(code: "requestWithoutFile"))]
		}
        return [responseVS : eventVSElectionService.saveEvent(messageSMIME, request.getLocale())]
	}

	/**
	 * Servicio de consulta de los votesVS
	 *
	 * @param [eventAccessControlURL] Obligatorio. URL del evento en el Control de Acceso.
	 * @httpMethod [GET]
	 * @responseContentType [application/json]
	 * @return Documento JSON con la lista de votesVS recibidos por la votación solicitada.
	 */
    def votes () {
        if (params.eventAccessControlURL) {
            def eventVSElection
			EventVS.withTransaction {
				eventVSElection = EventVS.findWhere(url:params.eventAccessControlURL)
			}
            if (eventVSElection) {
                def votesVSMap = new HashMap()
                votesVSMap.votesVS = []
                votesVSMap.fieldsEventVS = []
				List<VoteVS> votesVS
				VoteVS.withTransaction {
					votesVS = VoteVS.findAllWhere(eventVSElection:eventVSElection)
				}
				def votesVSCancelled = VoteVS.findAllWhere(eventVSElection:eventVSElection, state:VoteVS.State.CANCELLED)
				def votesVSOk = VoteVS.findAllWhere(eventVSElection:eventVSElection, VoteVS.State.OK)
                votesVSMap.numVotesVS = votesVS.size()
				votesVSMap.numVotesVSOK = votesVSOk.size()
				votesVSMap.numVotesVSVotesVSCANCELLED = votesVSCancelled.size()
                votesVSMap.accessControlURL=eventVSElection.accessControl.serverURL
				votesVSMap.eventVSElectionURL=eventVSElection.url
                HexBinaryAdapter hexConverter = new HexBinaryAdapter();
                votesVS.each {voteVS ->
                    String hashCertVoteHex = hexConverter.marshal(voteVS.getCertificateVS.hashCertVoteBase64.getBytes());
                    def voteVSMap = [id:voteVS.id, hashCertVoteBase64:voteVS.getCertificateVS.hashCertVoteBase64,
                        fieldEventVSId:voteVS.getFieldEventVS.fieldEventVSId, eventVSElectionId:voteVS.eventVS.eventVSElectionId,
                        state:voteVS.state,
						certificateURL:"${grailsApplication.config.grails.serverURL}/certificateVS/voteVS/hashHex/${hashCertVoteHex}",
                        voteVSSMIMEURL:"${grailsApplication.config.grails.serverURL}/messageSMIME/${voteVS.messageSMIME.id}"]
					if(VoteVS.State.CANCELLED == voteVS.state) {
						VoteVSCanceller voteVSCanceller
						VoteVSCanceller.withTransaction {
							voteVSCanceller = VoteVSCanceller.findWhere(voteVS:voteVS)
						}
						voteVSMap.cancellerURL="${grailsApplication.config.grails.serverURL}/messageSMIME/${voteVSCanceller?.messageSMIME?.id}"
					}
					votesVSMap.votesVS.add(voteVSMap)
                }
                eventVSElection.fieldsEventVS.each {fieldEventVS ->
					def numVotesVS = VoteVS.findAllWhere(optionSelected:fieldEventVS, state:VoteVS.State.OK).size()
                    def fieldEventVSMap = [fieldEventVSId:fieldEventVS.id,
                            content:fieldEventVS.content, numVotesVS:numVotesVS]
                    votesVSMap.fieldsEventVS.add(fieldEventVSMap)
                    
                }
				render votesVSMap as JSON
                return
            }
            return [responseVS : new ResponseVS(ResponseVS.SC_NOT_FOUND, message(code: 'eventVSUrlNotFound',
                    args:[params.eventAccessControlURL]))]
        }
        return [responseVS : new ResponseVS(statusCode: ResponseVS.SC_ERROR_REQUEST,
                contentType: ContentTypeVS.HTML, message: message(code: 'requestWithErrorsHTML',
                args:["${grailsApplication.config.grails.serverURL}/${params.controller}/restDoc"]))]
    }

	/**
	 * Servicio que ofrece datos de recuento de una votación.
	 * 
	 * @param [eventAccessControlURL] Obligatorio. URL del evento en el Control de Acceso.
	 *
	 * @httpMethod [GET]
	 * @serviceURL [/eventVS/$id/statistics]
	 * @param [id] Opcional. El identificador en la base de datos del evento consultado.
     * @param [eventAccessControlURL] Opcional. La url del evento en el Control de Asceso 
     *         que se publicó
	 * @responseContentType [application/json]
	 * @return Documento JSON con estadísticas de la votación solicitada.
	 */
    def statistics () {
		EventVS eventVSElection
		if (params.long('id')) {
            EventVS.withTransaction { eventVSElection = EventVS.get(params.long('id')) }
			if (!eventVSElection) {
                return [responseVS : new ResponseVS(ResponseVS.SC_NOT_FOUND,
                        message(code: 'eventVS.eventVSNotFound', args:[params.id]))]
			}
		} else if(params.eventAccessControlURL) {
			log.debug("params.eventAccessControlURL: ${params.eventAccessControlURL}")
			EventVS.withTransaction { eventVSElection = EventVS.findByUrl(params.eventAccessControlURL.trim()) }
			if (!eventVSElection) {
                return [responseVS : new ResponseVS(ResponseVS.SC_NOT_FOUND,
                        message(code: 'eventVS.eventVSNotFound', args:[params.eventAccessControlURL]))]
			}
		}
        if (eventVSElection) {
            response.status = ResponseVS.SC_OK
            def statisticsMap = new HashMap()
			statisticsMap.fieldsEventVS = []
            statisticsMap.id = eventVSElection.id
			statisticsMap.numVotesVS = VoteVS.countByEventVSElection(eventVSElection)
			statisticsMap.numVotesVSOK = VoteVS.countByEventVSElectionAndState(
					eventVSElection, VoteVS.State.OK)
			statisticsMap.numVotesVSVotesVSCANCELLED = VoteVS.countByEventVSElectionAndState(
				eventVSElection, VoteVS.State.CANCELLED)
            eventVSElection.fieldsEventVS.each { opcion ->
				def numVotesVS = VoteVS.countByOpcionDeEventoAndState(
					opcion, VoteVS.State.OK)
				def opcionMap = [id:opcion.id, content:opcion.content,
					numVotesVS:numVotesVS, fieldEventVSId:opcion.fieldEventVSId]
				statisticsMap.fieldsEventVS.add(opcionMap)
			}
			statisticsMap.voteVSInfoURL="${grailsApplication.config.grails.serverURL}/eventVS/votes?eventAccessControlURL=${eventVSElection.url}"
			if (params.callback) render "${params.callback}(${statisticsMap as JSON})"
			else render statisticsMap as JSON
        } else {
            return [responseVS : new ResponseVS(statusCode: ResponseVS.SC_ERROR_REQUEST,
                    contentType: ContentTypeVS.HTML, message: message(code: 'requestWithErrorsHTML',
                    args:["${grailsApplication.config.grails.serverURL}/${params.controller}/restDoc"]))]
		}
    }

	/**
	 * Servicio que comprueba las fechas de una votación
	 *
	 * @httpMethod [GET]
	 * @serviceURL [/eventVS/$id/checkDates]
	 * @param [id] Obligatorio. El identificador de la votación en la base de datos.	
	 */
    def checkDates () {
		EventVS eventVSElection
		if (params.long('id')) {
            EventVS.withTransaction { eventVSElection = EventVS.get(params.id) }
		}
		if(!eventVSElection) {
            return [responseVS : new ResponseVS(ResponseVS.SC_NOT_FOUND,
                    message(code: 'eventVSNotFoundErrorMsg', args:[params.id]))]
		}
        return [responseVS : eventVSElectionService.checkDatesEventVS(eventVSElection, request.getLocale())]
	}
	
	/**
	 * Servicio de cancelación de votaciones 
	 *
	 * @contentType [application/x-pkcs7-signature] Obligatorio. Archivo con los datos de la votación que se 
	 * 			desea cancelar firmado por el Control de Acceso que publicó la votación y por el usuario que
	 *          la publicó o un administrador de sistema.
	 * @httpMethod [POST]
	 */
	def cancelled() {
		MessageSMIME messageSMIMEReq = request.messageSMIMEReq
        if(!messageSMIMEReq) {
            return [responseVS:new ResponseVS(ResponseVS.SC_ERROR_REQUEST, message(code:'requestWithoutFile'))]
        }
		ResponseVS responseVS = eventVSElectionService.cancelEvent(
			messageSMIMEReq, request.getLocale());
		if(ResponseVS.SC_OK == responseVS.statusCode) {
			response.status = ResponseVS.SC_OK
            responseVS.setContentType(ContentTypeVS.SIGNED)
		}
        return [responseVS : responseVS]
	}
	
	
	/**
	 * Servicio que devuelve un archivo zip con los errores que se han producido
	 * en una votación
	 *
	 * @httpMethod [GET]
	 * @serviceURL [/eventVS/$id/votingErrors]
	 * @param [id] Obligatorio. Identificador del evento en la base de datos
	 * @return Archivo zip con los messages con errores
	 */
	def votingErrors() {
		EventVS event
		EventVS.withTransaction {
			event = EventVS.get(params.long('id'))
		}
		if (!event) {
            return [responseVS : new ResponseVS(ResponseVS.SC_NOT_FOUND,
                    message(code: 'eventVSNotFound', args:[params.id]))]
		}
		def errors
		MessageSMIME.withTransaction { errors = MessageSMIME.findAllWhere (type:TypeVS.VOTE_ERROR,  eventVS:event)}
		
		if(errors.size == 0){
            return [responseVS : new ResponseVS(ResponseVS.SC_OK,message(code: 'votingWithoutErrorsMsg',
                    args:[event.id, event.subject]))]
		} else {
			String datePathPart = DateUtils.getShortStringFromDate(event.getDateFinish())
			String baseDirPath = "${grailsApplication.config.VotingSystem.errorsBaseDir}" +
				"/${datePathPart}/Event_${event.id}"
			errors.each { messageSMIME ->
				File errorFile = new File("${baseDirPath}/MessageSMIME_${messageSMIME.id}")
				errorFile.setBytes(messageSMIME.content)
			}
			File zipResult = new File("${baseDirPath}.zip")
			def ant = new AntBuilder()
			ant.zip(destfile: zipResult, basedir: "${baseDirPath}")
            return [responseVS : new ResponseVS(statusCode:ResponseVS.SC_OK, messageBytes:zipResult.getBytes(),
                    contentType: ContentTypeVS.ZIP)]
		}
	}
}
