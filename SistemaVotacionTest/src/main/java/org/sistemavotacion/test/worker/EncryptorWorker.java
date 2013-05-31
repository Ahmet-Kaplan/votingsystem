package org.sistemavotacion.test.worker;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import org.sistemavotacion.worker.*;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.swing.SwingWorker;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.encoders.Base64;
import org.sistemavotacion.Contexto;
import org.sistemavotacion.modelo.Respuesta;
import org.sistemavotacion.seguridad.Encryptor;
import org.sistemavotacion.util.VotingSystemKeyGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
*/
public class EncryptorWorker extends SwingWorker<Respuesta, String> 
        implements VotingSystemWorker {
    
    private static Logger logger = LoggerFactory.getLogger(EncryptorWorker.class);

    private Integer id;
    private String urlRequest;
    private Respuesta respuesta = new Respuesta(Respuesta.SC_ERROR);
    private VotingSystemWorkerListener workerListener;
        
    private X509Certificate serverCert = null;
    
    private static final String BC = BouncyCastleProvider.PROVIDER_NAME;

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private String from = null;
    
    public EncryptorWorker(Integer id, String from, String urlRequest, 
            VotingSystemWorkerListener workerListener, X509Certificate serverCert) 
            throws OperatorCreationException {
        this.id = id;
        this.from = from;
        this.urlRequest = urlRequest;
        this.workerListener = workerListener;
        this.serverCert = serverCert;
        KeyPair keyPair = VotingSystemKeyGenerator.getInstancia().genKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
    }
    
    @Override protected Respuesta doInBackground() throws Exception {
        File encryptedFile = File.createTempFile("csrEncryptedFile", ".p7m");
        encryptedFile.deleteOnExit();
        String testJSONstr = getTestJSON(from, publicKey);
        Encryptor.encryptMessage(testJSONstr.getBytes(), encryptedFile, serverCert);
        
        respuesta = Contexto.getHttpHelper().sendFile(encryptedFile, 
                Contexto.ENCRYPTED_CONTENT_TYPE, urlRequest);

        if (Respuesta.SC_OK == respuesta.getCodigoEstado()) {
            byte[] encryptedData = respuesta.getBytesArchivo();
            byte[] decryptedData = Encryptor.decryptFile(encryptedData, 
                    publicKey, privateKey);
            //logger.debug(" >>>>>> decryptedData: " + new String(decryptedData));
        }
        return respuesta;
    }

    @Override protected void done() {//on the EDT
        try {
            respuesta = get();
        }catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            respuesta = new Respuesta(Respuesta.SC_ERROR, ex.getMessage());
        } 
        workerListener.showResult(this);
    }
    
    @Override//on the EDT
    protected void process(List<String> messages) {
        workerListener.process(messages);
    }

    public String getTestJSON(String from, PublicKey publicKey) {
        String result = null;
        try {
            logger.debug("getTestJSON");
            String publicKeyStr = new String(Base64.encode(publicKey.getEncoded()));
            
            //logger.debug("publicKeyStr: " + publicKeyStr);

            //byte[] decodedPK = Base64.decode(publicKeyStr);
            //PublicKey pk =  KeyFactory.getInstance("RSA").
            //        generatePublic(new X509EncodedKeySpec(decodedPK));
            //logger.debug("pk.toString(): " + pk.toString());
            
            Map map = new HashMap();
            map.put("from", from);
            map.put("publicKey", publicKeyStr);
            map.put("UUID", UUID.randomUUID().toString());
            JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(map);
            result = jsonObject.toString();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return result;
    }

   @Override public String getMessage() {
        if(respuesta == null) return null;
        else return respuesta.getMensaje();
    }

    @Override public int getId() {
        return this.id;
    }

    @Override  public int getStatusCode() {
        if(respuesta == null) return Respuesta.SC_ERROR;
        else return respuesta.getCodigoEstado();
    }
    
    @Override public Respuesta getRespuesta() {
        return respuesta;
    }

}
