package org.votingsystem.callable;

import org.votingsystem.model.ResponseVS;
import org.votingsystem.signature.smime.SMIMEMessage;
import org.votingsystem.signature.util.Encryptor;
import org.votingsystem.util.ContentTypeVS;
import org.votingsystem.util.HttpHelper;

import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
* License: https://github.com/votingsystem/votingsystem/wiki/Licencia
*/
public class SMIMESignedSender implements Callable<ResponseVS> {

    private static Logger log = Logger.getLogger(SMIMESignedSender.class.getSimpleName());

    private String urlToSendDocument;
    private String timeStampServerURL;
    private SMIMEMessage smimeMessage;
    private X509Certificate destinationCert = null;
    private KeyPair keypair;
    private ContentTypeVS cotentType;
    private String[] headers = null;
    
    public SMIMESignedSender(SMIMEMessage smimeMessage, String urlToSendDocument, String timeStampServerURL,
            ContentTypeVS cotentType, KeyPair keypair, X509Certificate destinationCert, String... headers) {
        this.smimeMessage = smimeMessage;
        this.urlToSendDocument = urlToSendDocument;
        this.timeStampServerURL = timeStampServerURL;
        this.cotentType = cotentType;
        this.keypair = keypair;
        this.destinationCert = destinationCert;
        this.headers = headers;
    }

    @Override public ResponseVS call() throws Exception {
        log.info("doInBackground - urlToSendDocument: " + urlToSendDocument);
        MessageTimeStamper timeStamper = new MessageTimeStamper(smimeMessage, timeStampServerURL);
        smimeMessage = timeStamper.call();
        byte[] messageToSendBytes = null;
        if(cotentType.isEncrypted()) messageToSendBytes = Encryptor.encryptSMIME(smimeMessage, destinationCert);
        else if(cotentType.isSigned()) messageToSendBytes = smimeMessage.getBytes();
        ResponseVS responseVS = HttpHelper.getInstance().sendData(messageToSendBytes, cotentType, urlToSendDocument, headers);
        ContentTypeVS responseContentType = responseVS.getContentType();
        try {
            if(responseContentType != null && responseContentType.isSignedAndEncrypted()) {
                SMIMEMessage signedMessage = Encryptor.decryptSMIME(
                        responseVS.getMessageBytes(), keypair.getPublic(), keypair.getPrivate());
                responseVS.setSMIME(signedMessage);
            } else if(responseContentType != null && responseContentType.isEncrypted()) {
                byte[] decryptedBytes = Encryptor.decryptMessage(responseVS.getMessageBytes(), keypair.getPrivate());
                responseVS.setMessageBytes(decryptedBytes);
            } else if(responseContentType != null && ResponseVS.SC_OK == responseVS.getStatusCode() &&
                    responseContentType.isSigned()) {
                responseVS.setSMIME(new SMIMEMessage(responseVS.getMessageBytes()));
            }
        } catch(Exception ex) {
            log.log(Level.SEVERE, ex.getMessage(), ex);
            responseVS.setStatusCode(ResponseVS.SC_ERROR);
            responseVS.appendMessage(ex.getMessage());
        }
        return responseVS;
    }

}