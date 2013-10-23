package org.sistemavotacion.controlacceso

import org.sistemavotacion.controlacceso.modelo.*

/**
 * @infoController Servicio de páginas .gsp
 * @descController Controlador que sirve páginas gsp. 
 *
 * @author jgzornoza
 * Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
 * */
class GspController {

	/**
	 * @httpMethod [GET]
	 * @serviceURL [/gsp/$pageName]
	 * @param [pageName] Obligatorio. Nombre de la página gsp en el directorio 'views/gsp'
	 * @responseContentType [text/html]
	 * @return La página gsp solicitada.
	 */
	def index() {
		if(params.pageName) render(view:params.pageName, model:params)
		else {
			response.status = Respuesta.SC_ERROR
			render "page name null"
			return false
		}
	}
	
}