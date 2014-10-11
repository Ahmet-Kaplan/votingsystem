package org.votingsystem.vicket.controller

import grails.converters.JSON
import org.votingsystem.model.ContentTypeVS
import org.votingsystem.model.MessageSMIME
import org.votingsystem.model.ResponseVS
import org.votingsystem.model.TypeVS
import org.votingsystem.signature.smime.SMIMEMessage
import org.votingsystem.util.DateUtils
import org.votingsystem.vicket.model.TransactionVS
import org.votingsystem.vicket.util.AsciiDocUtil

/**
 * @infoController Mensajes firmados
 * @descController Servicios relacionados con los messages firmados manejados por la
 *                 aplicación.
 *
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
class MessageSMIMEController {

    def userVSService

    /**
     * @httpMethod [GET]
     * @serviceURL [/messageSMIME/$id]
     * @param [id]	Obligatorio. Identificador del message en la base de datos
     * @return El message solicitado.
     */
    def index() {
        def messageSMIME = request.messageSMIME
        if(!messageSMIME) {
            MessageSMIME.withTransaction{ messageSMIME = MessageSMIME.get(params.long('id')) }
        }
        if (messageSMIME) {
            if(ContentTypeVS.TEXT != request.contentTypeVS) {
                request.messageSMIME = messageSMIME
                forward(action:"contentViewer")
                return false
            } else {
                return [responseVS : new ResponseVS(statusCode:ResponseVS.SC_OK, contentType:ContentTypeVS.TEXT_STREAM,
                        messageBytes:messageSMIME.content)]
            }
        } else return [responseVS:new ResponseVS(ResponseVS.SC_NOT_FOUND,
                message(code: 'messageSMIMENotFound', args:[params.id]))]
    }


    /**
     * Servicio que devuelve el recibo con el que el servidor respondió un message
     *
     * @httpMethod [GET]
     * @serviceURL [/messageSMIME/receipt/$requestMessageId]
     * @param [requestMessageId] Obligatorio. Identificador del message origen del recibo en la base de datos
     * @return El recibo asociado al message pasado como parámetro.
     */
    def receipt() {
        MessageSMIME messageSMIMEOri = null
        MessageSMIME messageSMIME = null
        MessageSMIME.withTransaction{
            messageSMIMEOri = MessageSMIME.get(params.long('requestMessageId'))
            if (messageSMIMEOri) {
                messageSMIME = MessageSMIME.findWhere(smimeParent:messageSMIMEOri, type: TypeVS.RECEIPT)
                if (messageSMIME) {
                    return [responseVS:new ResponseVS(statusCode: ResponseVS.SC_OK, contentType: ContentTypeVS.TEXT_STREAM,
                            messageBytes: messageSMIME.content)]
                }
            }
        }
        if(messageSMIMEOri && !messageSMIME) {
            return [responseVS : new ResponseVS(ResponseVS.SC_NOT_FOUND,
                    message(code: 'messageSMIMEWithoutReceiptMsg', args:[params.requestMessageId]))]
        } else return [responseVS : new ResponseVS(ResponseVS.SC_NOT_FOUND,
                message(code: 'messageSMIMENotFound', args:[params.requestMessageId]))]
    }


    def transactionVS() {
        TransactionVS transactionVS = null
        TransactionVS.withTransaction {
            transactionVS = TransactionVS.get(params.long('id'))
        }
        if(transactionVS) {
            request.messageSMIME = transactionVS.messageSMIME
            forward(action:"index")
        } else return [responseVS : new ResponseVS(ResponseVS.SC_NOT_FOUND,
                message(code: 'transactionVSNotFound', args:[params.id]))]
    }

    def contentViewer() {
        String viewer = "message-smime"
        String smimeMessageStr
        String timeStampDate
        boolean isAsciiDoc = false
        def signedContentJSON
        if(request.messageSMIME) {
            smimeMessageStr = Base64.getEncoder().encodeToString(request.messageSMIME.content)
            SMIMEMessage smimeMessage = request.messageSMIME.getSmimeMessage()
            if(smimeMessage.getTimeStampToken() != null) {
                timeStampDate = DateUtils.getDateStr(smimeMessage.getTimeStampToken().getTimeStampInfo().getGenTime());
            }
            if(smimeMessage.getContentTypeVS() == ContentTypeVS.ASCIIDOC) {
                signedContentJSON = JSON.parse(AsciiDocUtil.getMetaInfVS(
                        request.messageSMIME.getSmimeMessage()?.getSignedContent()))
                signedContentJSON.asciiDoc = request.messageSMIME.getSmimeMessage()?.getSignedContent()
                signedContentJSON.asciiDocHTML = AsciiDocUtil.getHTML(request.messageSMIME.getSmimeMessage()?.getSignedContent())
            } else {
                signedContentJSON = JSON.parse(request.messageSMIME.getSmimeMessage()?.getSignedContent())
            }
            if(TypeVS.VICKET_SEND != TypeVS.valueOf(signedContentJSON.operation)) {
                if(!signedContentJSON.fromUserVS) signedContentJSON.fromUserVS =
                        userVSService.getUserVSBasicDataMap(request.messageSMIME.userVS)
            }
            params.operation = signedContentJSON.operation
        }
        /*if(params.operation) {
            try {
                TypeVS operationType = TypeVS.valueOf(params.operation.toUpperCase())
                operationType = TypeVS.valueOf(params.operation.toUpperCase())
                switch(operationType) {
                    case TypeVS.TRANSACTIONVS_FROM_BANKVS:
                        viewer = "message-smime"
                        break;
                    case TypeVS.TRANSACTIONVS_FROM_GROUP_TO_ALL_MEMBERS:
                        viewer = "message-smime"
                        break;
                }
            } catch(Exception ex) { log.error(ex.getMessage(), ex)}
        }*/
        Map model = [operation:params.operation, smimeMessage:smimeMessageStr,
               viewer:viewer, signedContentMap:signedContentJSON, timeStampDate:timeStampDate]
        if(request.contentType?.contains("json")) {
            render model as JSON
        } else render(view:'contentViewer', model:model)
    }


    /**
     * If any method in this controller invokes code that will throw a Exception then this method is invoked.
     */
    def exceptionHandler(final Exception exception) {
        log.error "Exception occurred. ${exception?.message}", exception
        String metaInf = "EXCEPTION_${params.controller}Controller_${params.action}Action"
        return [responseVS:new ResponseVS(statusCode:ResponseVS.SC_ERROR_REQUEST, message: exception.getMessage(),
                metaInf:metaInf, type:TypeVS.ERROR, reason:exception.getMessage())]
    }
}