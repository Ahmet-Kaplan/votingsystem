package org.votingsystem.controlcenter.service

import grails.transaction.Transactional
import org.bouncycastle.asn1.ASN1OctetString
import org.bouncycastle.asn1.DERObject
import org.bouncycastle.asn1.cms.CMSAttributes
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber
import org.bouncycastle.cert.X509CertificateHolder
import org.bouncycastle.cms.CMSSignedData
import org.bouncycastle.cms.SignerInformation
import org.bouncycastle.cms.SignerInformationVerifier
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder
import org.bouncycastle.operator.DigestCalculator
import org.bouncycastle.tsp.TSPUtil
import org.bouncycastle.tsp.TimeStampToken
import org.codehaus.groovy.grails.web.json.JSONObject
import static org.springframework.context.i18n.LocaleContextHolder.*
import org.votingsystem.model.*
import org.votingsystem.signature.util.CertUtils
import org.votingsystem.throwable.ExceptionVS
import org.votingsystem.util.HttpHelper
import org.votingsystem.util.MetaInfMsg
import org.votingsystem.util.StringUtils

import java.security.MessageDigest
import java.security.cert.X509Certificate

@Transactional
class TimeStampService {
	
	def grailsApplication
	def messageSource

    private SignerInformationVerifier timeStampSignerInfoVerifier
    private byte[] signingCertPEMBytes

    public synchronized Map init() {
        log.debug("init");
        try {
            String serverURL = StringUtils.checkURL(grailsApplication.config.vs.urlTimeStampServer)
            ActorVS timeStampServer = ActorVS.findWhere(serverURL:serverURL)
            X509Certificate x509TimeStampServerCert = null;
            CertificateVS timeStampServerCert = null;
            if(!timeStampServer) {
                fetchTimeStampServerInfo(new ActorVS(serverURL:serverURL));
                return null
            } else {
                timeStampServerCert = CertificateVS.findWhere(actorVS:timeStampServer, state:CertificateVS.State.OK,
                        type:CertificateVS.Type.TIMESTAMP_SERVER)
                if(timeStampServerCert) {
                    x509TimeStampServerCert = CertUtils.loadCertificate(timeStampServerCert.content)
                    if(Calendar.getInstance().getTime().before(x509TimeStampServerCert.notAfter)) {
                        signingCertPEMBytes = CertUtils.getPEMEncoded(x509TimeStampServerCert)
                    } else {
                        log.debug("timeStampServerCert lapsed - not valid after: ${x509TimeStampServerCert.notAfter}")
                        timeStampServerCert.setState(CertificateVS.State.LAPSED).save()
                    }
                }
                if(!signingCertPEMBytes) {
                    fetchTimeStampServerInfo(timeStampServer);
                    return null
                }
            }
            if(x509TimeStampServerCert) {
                ContextVS.getInstance().setTimeStampServerCert(x509TimeStampServerCert)
                timeStampSignerInfoVerifier = new JcaSimpleSignerInfoVerifierBuilder().setProvider(
                        ContextVS.PROVIDER).build(x509TimeStampServerCert)
                X509CertificateHolder certHolder = timeStampSignerInfoVerifier.getAssociatedCertificate();
                TSPUtil.validateCertificate(certHolder);
            } else throw new Exception("TimeStamp signing cert for '${serverURL}' not found")
            return [timeStampSignerInfoVerifier:timeStampSignerInfoVerifier, signingCertPEMBytes:signingCertPEMBytes]
        } catch(Exception ex) {
            log.error(ex.getMessage(), ex)
        }
    }

    private void fetchTimeStampServerInfo(ActorVS timeStampServer) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        log.debug("$methodName - ${timeStampServer.serverURL}")
        runAsync {
            ResponseVS responseVS = HttpHelper.getInstance().getData(ActorVS.getServerInfoURL(timeStampServer.serverURL),
                    ContentTypeVS.JSON);
            X509Certificate x509TimeStampServerCert = null
            if(ResponseVS.SC_OK == responseVS.getStatusCode()) {
                ActorVS.withTransaction{
                    ActorVS newTimeStampServer = ActorVS.parse(new JSONObject(responseVS.getMessage()));
                    if(timeStampServer.serverURL.equals(newTimeStampServer.getServerURL())) {
                        if(!timeStampServer.id) {
                            timeStampServer = newTimeStampServer.save(flush:true);
                        } else timeStampServer.setCertChainPEM(newTimeStampServer.getCertChainPEM())
                        x509TimeStampServerCert = CertUtils.fromPEMToX509CertCollection(
                                timeStampServer.certChainPEM.getBytes()).iterator().next()
                        if(Calendar.getInstance().getTime().after(x509TimeStampServerCert.notAfter)) {
                            throw new ExceptionVS("${timeStampServer.serverURL} signing cert is lapsed")
                        }
                        log.debug("$methodName - updated TimeStampServer '${timeStampServer.id}' signing cert")
                    } else log.error("Expected server URL '${timeStampServer.serverURL}' but found " +
                            "'${newTimeStampServer.getServerURL()}'")
                }
                CertificateVS.withTransaction {
                    CertificateVS timeStampServerCert = new CertificateVS(actorVS:timeStampServer,
                            certChainPEM:timeStampServer.certChainPEM.getBytes(),
                            content:x509TimeStampServerCert?.getEncoded(),state:CertificateVS.State.OK,
                            serialNumber:x509TimeStampServerCert?.getSerialNumber()?.longValue(),
                            validFrom:x509TimeStampServerCert?.getNotBefore(), type:CertificateVS.Type.TIMESTAMP_SERVER,
                            validTo:x509TimeStampServerCert?.getNotAfter()).save();
                    log.debug("Added TimeStampServer Cert: ${timeStampServerCert.id}")
                }
            } else log.error("ERROR fetching TimeStampServerInfo : ${timeStampServer.serverURL}")
            init();
        }
    }

    public byte[] getSigningCertPEMBytes() {
        if(!signingCertPEMBytes) signingCertPEMBytes = init()?.signingCertPEMBytes
        return signingCertPEMBytes
    }

	public ResponseVS validateToken(TimeStampToken timeStampToken, EventVS eventVS) throws Exception {
        validateToken(timeStampToken)
        Date timestampDate = timeStampToken.getTimeStampInfo().getGenTime()
        if(!timestampDate.after(eventVS.dateBegin) || !timestampDate.before(eventVS.getDateFinish())) {
            String msg = messageSource.getMessage('timestampDateErrorMsg',
                    [timestampDate, eventVS.dateBegin, eventVS.getDateFinish()].toArray(), locale)
            log.debug("validateToken - ERROR TIMESTAMP DATE -  - Event '${eventVS.id}' - ${msg}")
            return new ResponseVS(statusCode:ResponseVS.SC_ERROR_REQUEST,
                    message:msg, eventVS:eventVS)
        } else return new ResponseVS(statusCode:ResponseVS.SC_OK);
	}

    public void validateToken(TimeStampToken tsToken) throws ExceptionVS {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
        if(tsToken == null) throw new ExceptionVS(messageSource.getMessage('documentWithoutTimeStampErrorMsg', null,
                locale), MetaInfMsg.getErrorMsg(methodName, 'timestampMissing'))
        SignerInformationVerifier sigVerifier = getTimeStampSignerInfoVerifier()
        if(!sigVerifier)throw new ExceptionVS("TimeStamp service not initialized")
        X509CertificateHolder certHolder = sigVerifier.getAssociatedCertificate();
        DigestCalculator calc = sigVerifier.getDigestCalculator(tsToken.certID.getHashAlgorithm());
        OutputStream cOut = calc.getOutputStream();
        cOut.write(certHolder.getEncoded());
        cOut.close();
        if (!Arrays.equals(tsToken.certID.getCertHash(), calc.getDigest())) {
            throw new ExceptionVS(messageSource.getMessage('certHashErrorMsg', null, locale))
        }
        if (tsToken.certID.getIssuerSerial() != null) {
            IssuerAndSerialNumber issuerSerial = certHolder.getIssuerAndSerialNumber();
            if (!tsToken.certID.getIssuerSerial().getSerial().equals(issuerSerial.getSerialNumber())) {
                throw new ExceptionVS(messageSource.getMessage('issuerSerialErrorMsg', null, locale))
            }
        }
        if (!certHolder.isValidOn(tsToken.tstInfo.getGenTime())) {
            throw new ExceptionVS(messageSource.getMessage('certificateDateError', null, locale))
        }
        CMSSignedData tokenCMSSignedData = tsToken.tsToken
        Collection signers = tokenCMSSignedData.getSignerInfos().getSigners();
        SignerInformation tsaSignerInfo = (SignerInformation)signers.iterator().next();

        DERObject validMessageDigest = tsaSignerInfo.getSingleValuedSignedAttribute(
                CMSAttributes.messageDigest, "message-digest");
        ASN1OctetString signedMessageDigest = (ASN1OctetString)validMessageDigest
        byte[] digestToken = signedMessageDigest.getOctets();

        String algorithmStr = TSPUtil.getDigestAlgName(
                tsToken.tsaSignerInfo.getDigestAlgorithmID().getAlgorithm().toString())

        ByteArrayOutputStream baos = new ByteArrayOutputStream()
        tsToken.tsaSignerInfo.content.write(baos);
        byte[] contentBytes = baos.toByteArray()
        MessageDigest sha = MessageDigest.getInstance(algorithmStr);
        byte[] resultDigest =  sha.digest(contentBytes);
        baos.close();
        if(!Arrays.equals(digestToken, resultDigest)) {
            String tokenStr = java.util.Base64.getEncoder().encodeToString(tsToken.getEncoded());
            String resultDigestStr =  java.util.Base64.getEncoder().encodeToString(resultDigest);
            String digestTokenStr = java.util.Base64.getEncoder().encodeToString(digestToken);
            throw new ExceptionVS("algorithmStr: '${algorithmStr} 'resultDigestStr '${resultDigestStr} - digestTokenStr '${digestTokenStr}'")
        }
    }

    public SignerInformationVerifier getTimeStampSignerInfoVerifier(){
        if(!timeStampSignerInfoVerifier) timeStampSignerInfoVerifier = init().timeStampSignerInfoVerifier
        return timeStampSignerInfoVerifier
    }

}
