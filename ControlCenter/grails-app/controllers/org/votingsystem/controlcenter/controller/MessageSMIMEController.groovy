package org.votingsystem.controlcenter.controller

import org.votingsystem.model.ContentTypeVS
import org.votingsystem.model.MessageSMIME;
import org.votingsystem.model.ResponseVS;

/**
 * @infoController Messages firmados
 * @descController Servicios relacionados con los messages firmados manejados por la
 *                 aplicación.
 * 
 * @author jgzornoza
 * Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
 */
class MessageSMIMEController {
	
	/**
	 * @httpMethod [GET]
	 * @serviceURL [/messageSMIME/$id] 
	 * @param [id]	Obligatorio. Identificador del message en la base de datos
	 * @return El message solicitado.
	 */
    def index () {
        def messageSMIME;
		MessageSMIME.withTransaction {
			messageSMIME = MessageSMIME.get(params.id)
		}
        if (messageSMIME) {
            params.responseVS = new ResponseVS(statusCode:ResponseVS.SC_OK, contentType:ContentTypeVS.TEXT_STREAM,
                    messageBytes:messageSMIME.content)
        } else params.responseVS = new ResponseVS(ResponseVS.SC_NOT_FOUND,
                message(code: 'eventVSNotFound', args:[params.id]))
    }
	
}