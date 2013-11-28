package org.votingsystem.simulation.callable

import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.json.JSONObject
import org.votingsystem.model.ActorVS
import org.votingsystem.model.ResponseVS
import org.votingsystem.signature.smime.SMIMEMessageWrapper
import org.votingsystem.signature.smime.SignedMailGenerator
import org.votingsystem.simulation.ContextService
import org.votingsystem.util.ApplicationContextHolder

import java.security.KeyStore
import java.util.concurrent.Callable
/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
*/
public class TimeStamperTestSender implements Callable<ResponseVS> {
    
    private static Logger logger = Logger.getLogger(TimeStamperTestSender.class);
    
    private SMIMEMessageWrapper documentSMIME;
    private String requestNIF;
    private String urlTimeStampService;
    private Long eventId;
    private ContextService contextService = null;

    public TimeStamperTestSender(String requestNIF, Long eventId)
            throws Exception {
        this.requestNIF = requestNIF;
        this.eventId = eventId;
        contextService = ApplicationContextHolder.getSimulationContext();
        this.urlTimeStampService = contextService.getAccessControl().getTimeStampServerURL();
    }
        
    @Override public ResponseVS call() throws Exception {
        KeyStore mockDnie = contextService.generateTestDNIe(requestNIF);

        ActorVS accessControl = contextService.getAccessControl();
        String toUser = accessControl.getNameNormalized();
        SignedMailGenerator signedMailGenerator = new SignedMailGenerator(mockDnie,
                contextService.END_ENTITY_ALIAS, contextService.PASSWORD.toCharArray(),
                contextService.VOTE_SIGN_MECHANISM);
        String subject = contextService.getMessage("timeStampMsgSubject");
        
        documentSMIME = signedMailGenerator.genMimeMessage(requestNIF, toUser, getRequestDataJSON(), subject , null);

        /*MessageTimeStamper timeStamper = new MessageTimeStamper(documentSMIME);
        ResponseVS responseVS = timeStamper.call();
        if(ResponseVS.SC_OK != responseVS.getStatusCode()) return responseVS;
        documentSMIME = timeStamper.getSmimeMessage();*/
        String urlTimeStampTestService = urlTimeStampService + "/test"
        SMIMESignedSender signedSender = new SMIMESignedSender(documentSMIME, urlTimeStampTestService, null, null);
        ResponseVS responseVS = signedSender.call();
        
        return responseVS;
    }
        
    public String getRequestDataJSON() {
        logger.debug("getRepresentativeDataJSOn - ");
        Map map = new HashMap();
        map.put("operation", "TIMESTAMP_TEST");
        map.put("UUID", UUID.randomUUID().toString());
        map.put("eventId", eventId);
        JSONObject jsonObject = new JSONObject(map);
        return jsonObject.toString();
    }

}