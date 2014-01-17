package org.votingsystem.android.callable;

import android.content.Context;
import android.util.Log;

import org.votingsystem.android.R;
import org.votingsystem.model.ContentTypeVS;
import org.votingsystem.model.ContextVS;
import org.votingsystem.model.ResponseVS;
import org.votingsystem.signature.smime.SMIMEMessageWrapper;
import org.votingsystem.signature.util.CertificationRequestVS;
import org.votingsystem.signature.util.Encryptor;
import org.votingsystem.signature.util.VotingSystemKeyStoreException;
import org.votingsystem.util.HttpHelper;

import java.security.cert.X509Certificate;
import java.util.concurrent.Callable;

import javax.mail.Header;

/**
 * @author jgzornoza
 * Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
 */
public class AnonymousSMIMESender implements Callable<ResponseVS> {

    public static final String TAG = "AnonymousSMIMESender";

    private Context context;
    private ContextVS contextVS;
    private CertificationRequestVS certificationRequest;
    private String fromUser;
    private String toUser;
    private String textToSign;
    private String subject;
    private String serviceURL;
    private X509Certificate receiverCert;
    private ContentTypeVS contentType;
    private Header header;

    public AnonymousSMIMESender(String fromUser, String toUser, String textToSign, String subject,
            Header header, String serviceURL, X509Certificate receiverCert,
            ContentTypeVS contentType,CertificationRequestVS certificationRequest,Context context) {
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.textToSign = textToSign;
        this.subject = subject;
        this.header = header;
        this.serviceURL = serviceURL;
        this.receiverCert = receiverCert;
        this.context = context;
        this.contentType = contentType;
        this.certificationRequest = certificationRequest;
        contextVS = ContextVS.getInstance(context);
    }

    @Override public ResponseVS call() {
        Log.d(TAG + ".call()", "");
        ResponseVS responseVS = null;
        try {
            SMIMEMessageWrapper signedMessage = certificationRequest.genMimeMessage(fromUser, toUser,
                    textToSign, subject, header);
            MessageTimeStamper timeStamper = new MessageTimeStamper(signedMessage, context);
            responseVS = timeStamper.call();
            if(ResponseVS.SC_OK != responseVS.getStatusCode()) {
                responseVS.setStatusCode(ResponseVS.SC_ERROR_TIMESTAMP);
                return responseVS;
            }
            signedMessage = timeStamper.getSmimeMessage();
            byte[] messageToSend = Encryptor.encryptSMIME(signedMessage, receiverCert);
            responseVS = HttpHelper.sendData(messageToSend, contentType, serviceURL);
            if(ResponseVS.SC_OK == responseVS.getStatusCode()) {
                SMIMEMessageWrapper receipt = Encryptor.decryptSMIMEMessage(
                        responseVS.getMessageBytes(), certificationRequest.getKeyPair().getPublic(),
                        certificationRequest.getKeyPair().getPrivate());
                responseVS.setSmimeMessage(receipt);
            } else return responseVS;
        } catch(VotingSystemKeyStoreException ex) {
            ex.printStackTrace();
            responseVS = ResponseVS.getExceptionResponse(ex.getMessage(),
                    context.getString(R.string.exception_lbl));
        } catch(Exception ex) {
            ex.printStackTrace();
            responseVS = ResponseVS.getExceptionResponse(ex.getMessage(),
                    context.getString(R.string.exception_lbl));
        } finally { return responseVS; }
    }

}