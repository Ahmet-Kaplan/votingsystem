package org.sistemavotacion.controlacceso

import org.sistemavotacion.controlacceso.modelo.*;
import org.sistemavotacion.util.FileUtils;
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import grails.converters.JSON

class SolicitudCopiaController {
	
	def pdfService

	def index() {}
	
	def obtener() {
		if (params.long('id')) {
			SolicitudCopia solicitud = SolicitudCopia.get(params.id)
			if(!solicitud) {
				response.status = Respuesta.SC_ERROR_PETICION
				render message(code: 'solicitudCopia.noEncontrada', args:[params.id]) 
				return false
			}
			if(!solicitud.filePath) {
				response.status = Respuesta.SC_ERROR_PETICION
				render "La solicitud con id '${params.id}' ya ha sido descargada, solicite otra copia"
				return false
			}
			File copiaRespaldo = new File(solicitud.filePath)
			if (copiaRespaldo != null) {
				def bytesCopiaRespaldo = FileUtils.getBytesFromFile(copiaRespaldo)
				response.contentLength = bytesCopiaRespaldo.length
				response.setHeader("Content-disposition", "filename=${copiaRespaldo.getName()}")
				response.setHeader("NombreArchivo", "${copiaRespaldo.getName()}")
				response.setContentType("application/octet-stream")
				response.outputStream << bytesCopiaRespaldo
				response.outputStream.flush()
				solicitud.filePath = null
				solicitud.save()
				copiaRespaldo.delete()
				return false
			} else {
				log.error (message(code: 'error.SinCopiaRespaldo'))
				response.status = Respuesta.SC_ERROR_EJECUCION
				render message(code: 'error.SinCopiaRespaldo')
				return false
			}
		}
		response.status = Respuesta.SC_ERROR_PETICION
		render message(code: 'error.PeticionIncorrectaHTML', 
			args:["${grailsApplication.config.grails.serverURL}/${params.controller}"])
		return false
	}
	
	def validarSolicitud() {
		try {
			String nombreArchivo = ((MultipartHttpServletRequest) request)?.getFileNames()?.next();
			log.debug "Recibido archivo: ${nombreArchivo}"
			MultipartFile multipartFile = ((MultipartHttpServletRequest) request)?.getFile(nombreArchivo);
			if (multipartFile?.getBytes() != null || params.archivoFirmado) {
				Respuesta respuesta = pdfService.validarSolicitudCopia(
					multipartFile.getBytes(), request.getLocale())
				if (Respuesta.SC_OK != respuesta.codigoEstado) {
					log.debug "Problemas procesando solicitud de copia de seguridad - ${respuesta.mensaje}"
				}
				response.status = respuesta.codigoEstado
				render respuesta.mensaje
				return false
			}
		} catch (Exception ex) {
			log.error (ex.getMessage(), ex)
			response.status = Respuesta.SC_ERROR_PETICION
			render(ex.getMessage())
			return false
		}
	}
	
	def obtenerSolicitud() {
		if (params.long('id')) {
			SolicitudCopia solicitud
			byte[] solicitudPDF
			SolicitudCopia.withTransaction {
				solicitud = SolicitudCopia.get(params.id)
				if(solicitud) solicitudPDF = solicitud.documento.pdf
			}
			if(!solicitud) {
				response.status = Respuesta.SC_ERROR_PETICION
				render message(code: 'solicitudCopia.noEncontrada', args:[params.id]) 
				return false
			}
			//response.setHeader("Content-disposition", "attachment; filename=manifiesto.pdf")
			response.contentType = "application/pdf"
			response.setHeader("Content-Length", "${solicitudPDF.length}")
			response.outputStream << solicitudPDF // Performing a binary stream copy
			response.outputStream.flush()
			return false
		}
		response.status = Respuesta.SC_ERROR_PETICION
		render message(code: 'error.PeticionIncorrectaHTML', 
			args:["${grailsApplication.config.grails.serverURL}/${params.controller}"])
		return false
	}

}