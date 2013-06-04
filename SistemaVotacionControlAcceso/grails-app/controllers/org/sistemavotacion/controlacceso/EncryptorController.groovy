package org.sistemavotacion.controlacceso

import java.io.BufferedReader;
import java.io.FileReader;
import java.security.Key
import java.security.KeyPair;
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

import org.bouncycastle.openssl.PEMReader;
import org.bouncycastle.openssl.PEMWriter
import org.sistemavotacion.controlacceso.modelo.MensajeSMIME
import org.sistemavotacion.controlacceso.modelo.Respuesta
import grails.converters.JSON;
import java.io.BufferedReader
import org.bouncycastle.util.encoders.Base64;
import java.security.KeyFactory;
import grails.util.Environment

class EncryptorController {
	
	def grailsApplication

    def index() { 
		if(! Environment.DEVELOPMENT.equals(Environment.current)) {
			String msg = message(code: "serviceDevelopmentModeMsg")
			log.error msg
			response.status = Respuesta.SC_ERROR_PETICION
			render msg
			return false
		}
		if(!params.requestBytes) {
			String msg = message(code:'evento.peticionSinArchivo')
			log.error msg
			response.status = Respuesta.SC_ERROR_PETICION
			render msg
			return false
		}
		log.debug "===============****¡¡¡¡¡ DEVELOPMENT Environment !!!!!****=================== "
		byte[] solicitud = params.requestBytes
		//log.debug("Solicitud" + new String(solicitud))
		
		def mensajeJSON = JSON.parse(new String(solicitud))
		
		if(!mensajeJSON.publicKey) {
			String msg = message(code: "publicKeyMissingErrorMsg")
			log.error msg
			response.status = Respuesta.SC_ERROR_PETICION
			render msg
			return false
		}
		
	    byte[] decodedPK = Base64.decode(mensajeJSON.publicKey);
	    PublicKey receiverPublic =  KeyFactory.getInstance("RSA").
	            generatePublic(new X509EncodedKeySpec(decodedPK));
	    //log.debug("receiverPublic.toString(): " + receiverPublic.toString());
		
		mensajeJSON.message="Hello '${mensajeJSON.from}' from server"
		
		params.receiverPublicKey = receiverPublic
		response.setContentType("multipart/encrypted")
		
		params.respuesta = new Respuesta(codigoEstado:Respuesta.SC_OK)
		
		params.responseBytes = mensajeJSON.toString().getBytes()
		
	}
	
	private getPemBytesFromKey(Key key) {
		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
		PEMWriter pemWrt = new PEMWriter(new OutputStreamWriter(bOut));
		pemWrt.writeObject(key);
		pemWrt.close();
		bOut.close();
		return bOut.toByteArray()
	}
}