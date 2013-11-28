package org.votingsystem.accesscontrol.service

import org.votingsystem.model.CertificateVS
import org.votingsystem.model.ControlCenterVS
import org.votingsystem.model.UserVS
import org.votingsystem.model.ActorVS
import org.votingsystem.signature.util.CertUtil

import java.security.cert.X509Certificate
/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
*/
class UserVSService {
	
	static transactional = true
	
	List<String> administradoresSistema
	def grailsApplication

	public Map getUsuario(Date fromDate){
		def usersVS
		UserVS.withTransaction {
			def criteria = UserVS.createCriteria()
			usersVS = criteria.list {
				gt("dateCreated", fromDate)
			}
		}
		int numUsu = UserVS.countByDateCreatedGreaterThan(fromDate)

		Map datosRespuesta = [totalNumUsu:numUsu]
		return datosRespuesta
	}
	
	public Map getControlCenterMap(ControlCenterVS controlCenter) {
		CertificateVS certificate = CertificateVS.findWhere(actorVS:controlCenter, state:CertificateVS.State.OK)
		String certChainPEM
		if(certificate?.certChainPEM) {
			certChainPEM = new String(certificate.certChainPEM)
		}
		return [certChainPEM:certChainPEM, name:controlCenter.name,
			serverURL:controlCenter.serverURL, id:controlCenter.id, 
			serverType:ActorVS.Type.CONTROL_CENTER.toString()]
	}
	
	boolean isUserAdmin(String nif) {
		if(!administradoresSistema) {
			administradoresSistema = Arrays.asList(
			"${grailsApplication.config.VotingSystem.adminsDNI}".split(","))
		}
		return administradoresSistema.contains(nif)
	}
}
