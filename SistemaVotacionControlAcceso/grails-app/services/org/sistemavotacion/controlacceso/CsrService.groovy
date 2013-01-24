package org.sistemavotacion.controlacceso

import org.bouncycastle.asn1.pkcs.CertificationRequestInfo;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import org.sistemavotacion.controlacceso.modelo.*;
import org.sistemavotacion.seguridad.*
import org.sistemavotacion.util.*
import java.util.Locale;

class CsrService {
	
	def grailsApplication
	def subscripcionService
	def messageSource

	
    public Respuesta validarCSRVoto(byte[] csrPEMBytes, 
		EventoVotacion evento, Locale locale) {
        PKCS10CertificationRequest csr = PKCS10WrapperServer.fromPEMToPKCS10CertificationRequest(csrPEMBytes);
        CertificationRequestInfo info = csr.getCertificationRequestInfo();
		String eventoId;
		String controlAccesoURL;
		String hashCertificadoVotoHEX;
		String hashCertificadoVotoBase64;
        String subjectDN = info.getSubject().toString();
        log.debug("validarCSRVoto - subject: " + subjectDN)
		Respuesta respuesta = new Respuesta(codigoEstado:200, tipo: Tipo.OK)
		if(subjectDN.split("OU=eventoId:").length > 1) {
			eventoId = subjectDN.split("OU=eventoId:")[1].split(",")[0];
			if (!eventoId.equals(String.valueOf(evento.getId()))) {
				respuesta.codigoEstado = 400
				respuesta.mensaje = messageSource.getMessage('evento.solicitudCsrError', null, locale)
				log.error(respuesta.mensaje)
				return respuesta
			}
		}
		if(subjectDN.split("CN=controlAccesoURL:").length > 1) {
			String parte = subjectDN.split("CN=controlAccesoURL:")[1];
			if (parte.split(",").length > 1) {
				controlAccesoURL = parte.split(",")[0];
			} else controlAccesoURL = parte;
			controlAccesoURL = org.sistemavotacion.utils.StringUtils.checkURL(controlAccesoURL)	
			String serverURL = grailsApplication.config.grails.serverURL
			if (!serverURL.equals(controlAccesoURL)) {
				respuesta.codigoEstado = 400
				respuesta.mensaje = messageSource.getMessage(
					'error.urlControlAccesoWrong', [controlAccesoURL].toArray(), locale)
				log.error(respuesta.mensaje)
				return respuesta
			}	
		}
		if (subjectDN.split("OU=hashCertificadoVotoHEX:").length > 1) {
			try {
				hashCertificadoVotoHEX = subjectDN.split("OU=hashCertificadoVotoHEX:")[1].split(",")[0];
				HexBinaryAdapter hexConverter = new HexBinaryAdapter();
				hashCertificadoVotoBase64 = new String(hexConverter.unmarshal(hashCertificadoVotoHEX));
				log.debug("hashCertificadoVotoBase64: ${hashCertificadoVotoBase64}")
				SolicitudCSRVoto solicitudCSR = SolicitudCSRVoto.findWhere(
					hashCertificadoVotoBase64:hashCertificadoVotoBase64)
				respuesta.hashCertificadoVotoBase64 = hashCertificadoVotoBase64
				if (solicitudCSR) {
					respuesta.codigoEstado = 400
					respuesta.mensaje = messageSource.getMessage(
						'error.hashCertificadoVotoRepetido', [hashCertificadoVotoBase64].toArray(), locale)
					log.error(respuesta.mensaje)
					return respuesta
				}
			} catch (Exception ex) {
				log.error(ex.getMessage(), ex)
				return new Respuesta(codigoEstado:400, mensaje:ex.getMessage())
			}
		}
		return respuesta
    }
	
	public Respuesta validarCSRUsuario(byte[] csrPEMBytes, Locale locale) {
		PKCS10CertificationRequest csr = PKCS10WrapperServer.fromPEMToPKCS10CertificationRequest(csrPEMBytes);
		CertificationRequestInfo info = csr.getCertificationRequestInfo();
		String nif;
		String email;
		String telefono;
		String deviceId;
		String subjectDN = info.getSubject().toString();
		log.debug("validarCSRUsuario - subject: " + subjectDN)
		if(subjectDN.split("OU=email:").length > 1) {
			email = subjectDN.split("OU=email:")[1].split(",")[0];
		}
		if(subjectDN.split("CN=nif:").length > 1) {
			nif = subjectDN.split("CN=nif:")[1];
			if (nif.split(",").length > 1) {
				nif = nif.split(",")[0];
			}
		}
		if (subjectDN.split("OU=telefono:").length > 1) {
			telefono = subjectDN.split("OU=telefono:")[1].split(",")[0];
		}
		if (subjectDN.split("OU=deviceId:").length > 1) {
			deviceId = subjectDN.split("OU=deviceId:")[1].split(",")[0];
			log.debug("Con deviceId: ${deviceId}")
		} else log.debug("Sin deviceId")
		Respuesta respuesta = subscripcionService.comprobarDispositivo(
			nif, telefono, email, deviceId, locale)
		if(Respuesta.SC_OK != respuesta.codigoEstado) return respuesta;
		SolicitudCSRUsuario solicitudCSR
		def solicitudesPrevias = SolicitudCSRUsuario.findAllByDispositivoAndUsuarioAndEstado(
			respuesta.dispositivo, respuesta.usuario, SolicitudCSRUsuario.Estado.PENDIENTE_APROVACION)
		solicitudesPrevias.collect {eventoItem ->
			eventoItem.estado = SolicitudCSRUsuario.Estado.ANULADA
			eventoItem.save();
		}
		SolicitudCSRUsuario.withTransaction {
			solicitudCSR = new SolicitudCSRUsuario(
				estado:SolicitudCSRUsuario.Estado.PENDIENTE_APROVACION,
				contenido:csrPEMBytes, usuario:respuesta.usuario,
				dispositivo:respuesta.dispositivo).save()
		}
		if(solicitudCSR) return new Respuesta(codigoEstado:200, mensaje:solicitudCSR.id)
		else return new Respuesta(codigoEstado:400)
	}
}