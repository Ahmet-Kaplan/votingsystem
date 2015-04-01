package org.votingsystem.web.accesscontrol.ejb;

import org.votingsystem.model.*;
import org.votingsystem.signature.smime.SMIMEMessage;
import org.votingsystem.signature.util.CMSUtils;
import org.votingsystem.throwable.ValidationExceptionVS;
import org.votingsystem.util.*;
import org.votingsystem.web.cdi.ConfigVS;
import org.votingsystem.web.ejb.DAOBean;
import org.votingsystem.web.ejb.SignatureBean;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import java.io.ByteArrayInputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
@Stateless
public class VoteVSBean {

    private static Logger log = Logger.getLogger(VoteVSBean.class.getSimpleName());

    @Inject private ConfigVS config;
    @Inject private DAOBean dao;
    @Inject private SignatureBean signatureBean;


    public synchronized VoteVS validateVote(MessageSMIME messageSMIME) throws Exception {
        EventVSElection eventVS = (EventVSElection) messageSMIME.getEventVS();
        VoteVS voteVSRequest = messageSMIME.getSMIME().getVoteVS();
        CertificateVS voteVSCertificate = voteVSRequest.getCertificateVS();
        FieldEventVS optionSelected = eventVS.checkOptionId(voteVSRequest.getOptionSelected().getId());
        if (optionSelected == null) throw new ValidationExceptionVS(config.get("voteOptionNotFoundErrorMsg",
                voteVSRequest.getOptionSelected().getId().toString()));
        String fromUser = config.getServerName();
        String toUser = eventVS.getControlCenterVS().getName();
        String subject = config.get("voteValidatedByAccessControlMsg");
        SMIMEMessage smimeMessageResp = signatureBean.getSMIMEMultiSigned(
                fromUser, toUser, messageSMIME.getSMIME(), subject);
        messageSMIME.setType(TypeVS.ACCESS_CONTROL_VALIDATED_VOTE).setSMIME(smimeMessageResp);
        dao.merge(voteVSCertificate.setState(CertificateVS.State.USED));
        VoteVS voteVS = dao.persist(new VoteVS(optionSelected, eventVS, VoteVS.State.OK, voteVSCertificate,  messageSMIME));
        return voteVS;
    }


    public synchronized VoteVSCanceler processCancel (MessageSMIME messageSMIME) throws Exception {
        UserVS signer = messageSMIME.getUserVS();
        SMIMEMessage smimeMessage = messageSMIME.getSMIME();
        VoteVSRequest request = messageSMIME.getSignedContent(VoteVSRequest.class);
        request.validateCancelationRequest();
        Query query = dao.getEM().createQuery("select a from AccessRequestVS a where a.hashAccessRequestBase64 =:hashAccessRequestBase64 and " +
                "a.state =:state").setParameter("hashAccessRequestBase64", request.hashAccessRequestBase64)
                .setParameter("state", AccessRequestVS.State.OK);
        AccessRequestVS accessRequestVS = dao.getSingleResult(AccessRequestVS.class, query);
        if (accessRequestVS == null) throw new ValidationExceptionVS(config.get(
                "voteCancellationAccessRequestNotFoundError"));
        query = dao.getEM().createQuery("select c from CertificateVS c where c.hashCertVSBase64 =:hashCertVSBase64 and " +
                "c.state =:state").setParameter("hashCertVSBase64", request.hashCertVSBase64)
                .setParameter("state", CertificateVS.State.OK);
        CertificateVS certificateVS = dao.getSingleResult(CertificateVS.class, query);
        if (certificateVS == null) throw new ValidationExceptionVS(config.get(
                "voteCancellationCsrRequestNotFoundError"));
        query = dao.getEM().createQuery("select v from VoteVS v where v.certificateVS =:certificateVS " +
                "and v.state =:state").setParameter("certificateVS", certificateVS).setParameter("state", VoteVS.State.OK);
        VoteVS voteVS = dao.getSingleResult(VoteVS.class, query);
        if(voteVS == null) throw new ValidationExceptionVS("VoteVS not found");
        Date timeStampDate = signer.getTimeStampToken().getTimeStampInfo().getGenTime();
        if(!certificateVS.getEventVS().isActive(timeStampDate)) throw new ValidationExceptionVS(config.get(
                "timestampDateErrorMsg", DateUtils.getDateStr(timeStampDate),
                DateUtils.getDateStr(certificateVS.getEventVS().getDateBegin()),
                DateUtils.getDateStr(certificateVS.getEventVS().getDateFinish())));
        String toUser = certificateVS.getEventVS().getControlCenterVS().getName();
        String subject = config.get("voteCancelationSubject");
        smimeMessage.setMessageID(format("{0}/messageSMIME/id/{1}", config.getRestURL(), messageSMIME.getId()));
        SMIMEMessage smimeMessageReq = signatureBean.getSMIMEMultiSigned(toUser, smimeMessage, subject);
        String controlCenterURL = certificateVS.getEventVS().getControlCenterVS().getServerURL();
        String eventURL = format("{0}/eventVSElection/id/{1}", config.getRestURL(), certificateVS.getEventVS().getId());
        String voteCancelerURL = format("{0}/rest/voteVSCanceler?url={1}", controlCenterURL, eventURL);
        ResponseVS responseVSControlCenter = HttpHelper.getInstance().sendData(smimeMessageReq.getBytes(),
                ContentTypeVS.JSON_SIGNED, voteCancelerURL);
        if (ResponseVS.SC_OK == responseVSControlCenter.getStatusCode()) {
            SMIMEMessage smimeMessageResp = new SMIMEMessage(new ByteArrayInputStream(
                    responseVSControlCenter.getMessageBytes()));
            smimeMessageResp.isValidSignature();
            if(!smimeMessage.getContent().equals(smimeMessageResp.getContentDigestStr())) throw new ValidationExceptionVS(
                    "smimeContentMismatchError");
            signatureBean.validateSignersCerts(smimeMessageResp);
            dao.merge(messageSMIME.setType(TypeVS.CANCEL_VOTE).setSMIME(smimeMessageResp));
        } else {
            messageSMIME.setType(TypeVS.CANCEL_VOTE_ERROR).setEventVS(certificateVS.getEventVS()).setReason(
                    responseVSControlCenter.getMessage());
            dao.merge(messageSMIME);
            throw new ValidationExceptionVS(responseVSControlCenter.getMessage());
        }
        VoteVSCanceler voteCanceler = new VoteVSCanceler(messageSMIME, accessRequestVS,
                VoteVSCanceler.State.CANCELLATION_OK, request.originHashAccessRequest, request.hashCertVSBase64,
                request.originHashCertVote, request.hashCertVSBase64, voteVS);
        dao.persist(voteCanceler);
        dao.merge(voteVS.setState(VoteVS.State.CANCELED));
        dao.merge(accessRequestVS.setState(AccessRequestVS.State.CANCELED));
        dao.merge(certificateVS.setState(CertificateVS.State.CANCELED));
        return voteCanceler;
    }


    private class VoteVSRequest {
        String originHashCertVote, hashCertVSBase64, originHashAccessRequest, hashAccessRequestBase64;
        TypeVS operation;

        public VoteVSRequest(String signedContent) {}

        public void validateCancelationRequest() throws ValidationExceptionVS, NoSuchAlgorithmException {
            if(operation == null || TypeVS.CANCEL_VOTE != operation) throw new ValidationExceptionVS(
                    "ERROR - expected operation 'CANCEL_VOTE' - found: " + operation);
            if(originHashCertVote == null) throw new ValidationExceptionVS("ERROR - missing param 'originHashCertVote'");
            if(hashCertVSBase64 == null) throw new ValidationExceptionVS("ERROR - missing param 'hashCertVSBase64'");
            if(hashAccessRequestBase64 == null) throw new ValidationExceptionVS("ERROR - missing param 'hashAccessRequestBase64'");
            if(originHashAccessRequest == null) throw new ValidationExceptionVS("ERROR - missing param 'originHashAccessRequest'");
            if(originHashAccessRequest == null) throw new ValidationExceptionVS("ERROR - missing param 'originHashAccessRequest'");
            if(!hashAccessRequestBase64.equals(CMSUtils.getHashBase64(originHashAccessRequest,
                    ContextVS.VOTING_DATA_DIGEST))) throw new ValidationExceptionVS(config.get(
                    "voteCancellationAccessRequestHashError"));
            if(!hashCertVSBase64.equals(CMSUtils.getHashBase64(originHashCertVote,
                    ContextVS.VOTING_DATA_DIGEST))) throw new ValidationExceptionVS(
                    config.get("voteCancellationHashCertificateError"));
        }
    }

}