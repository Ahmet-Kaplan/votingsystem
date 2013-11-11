package org.votingsystem.accesscontrol.service

import java.security.KeyStore
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.votingsystem.accesscontrol.model.*;
import org.votingsystem.model.ResponseVS;
import org.votingsystem.signature.util.*
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource

import java.security.cert.Certificate

import org.votingsystem.util.*
/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
*/
class AlmacenClavesService {
	
	def grailsApplication
	def certificateService
 
    def generar(EventoVotacion evento) {
		ResponseVS respuesta
		AlmacenClaves almacenClavesPrevio = AlmacenClaves.findWhere(
			activo:Boolean.TRUE, evento:evento)
		if (almacenClavesPrevio) {
			log.error ("Ya se había generado el almacén de claves de CA del evento: '${evento.getId()}'")
			return new ResponseVS(statusCode:ResponseVS.SC_ERROR_PETICION)
		} 
		//String password = (1..7).inject("") { a, b -> a += ('a'..'z')[new Random().nextFloat() * 26 as int] }.toUpperCase()
		//TODO ====== crypto token
		String password = "${grailsApplication.config.VotingSystem.signKeysPassword}"
		String eventoUrl = "${grailsApplication.config.grails.serverURL}/evento/${evento.id}";
		String strSubjectDNRoot = "CN=eventoUrl:${eventoUrl}, OU=Votaciones";
		String aliasClavesFirma = grailsApplication.config.VotingSystem.signKeysAlias
		KeyStore keyStore = KeyStoreUtil.createRootKeyStore(evento.fechaInicio,
			evento.fechaFin, password.toCharArray(),
			aliasClavesFirma, null, strSubjectDNRoot);
		Certificate[] chain = keyStore.getCertificateChain(aliasClavesFirma);
		Certificate cert = chain[0]
		Certificado certificado = new Certificado(type:Certificado.Type.RAIZ_VOTOS,
			contenido:cert.getEncoded(), estado:Certificado.Estado.OK,
			numeroSerie:cert.getSerialNumber().longValue(),
			validoDesde:cert.getNotBefore(), validoHasta:cert.getNotAfter(),
			eventoVotacion:evento)
		certificado.save();
		AlmacenClaves almacenClaves = new AlmacenClaves (esRaiz:Boolean.TRUE, 
			activo:Boolean.TRUE, keyAlias:aliasClavesFirma,
			evento:evento, validoDesde:evento.fechaInicio,
			validoHasta:evento.fechaFin,
			bytes: KeyStoreUtil.getBytes(keyStore, password.toCharArray()))
		almacenClaves.save()
		respuesta = new ResponseVS(statusCode:ResponseVS.SC_OK, certificado:cert)
		log.debug ("Saved AlmacenClaves '${almacenClaves.getId()}' for event '${evento.getId()}'")
		return respuesta
    }
	
	def anular (EventoVotacion evento) {
		AlmacenClaves almacenClavesPrevio = AlmacenClaves.findWhere(
			activo:Boolean.TRUE, evento:evento)
		if (!almacenClavesPrevio) {
			log.debug ("El '${evento.getId()}' no tiene asociado ningún almacén de claves activo")
			return null
		}
		almacenClavesPrevio.activo = false;
		almacenClavesPrevio.save()
	}
	
	def anularRegenerando (EventoVotacion evento) {
		anular(evento);
		return generar(evento);
	}

}