package org.votingsystem.simulation.util

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.conn.ClientConnectionManager
import org.apache.http.conn.HttpHostConnectException
import org.apache.http.entity.ByteArrayEntity
import org.apache.http.entity.ContentType
import org.apache.http.entity.FileEntity
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.params.BasicHttpParams
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpParams
import org.apache.http.util.EntityUtils
import org.apache.log4j.Logger
import org.votingsystem.model.ResponseVS
import org.votingsystem.signature.util.CertUtil

import java.security.cert.PKIXParameters
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate
import java.text.ParseException
import java.util.concurrent.TimeUnit
import org.votingsystem.util.ApplicationContextHolder;
/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
*/
public class HttpHelper {
    
	private static Logger log = Logger.getLogger(
		HttpHelper.class);
    
    private HttpClient httpclient;
    private PoolingClientConnectionManager cm;
    private IdleConnectionEvictor connEvictor;
    private static HttpHelper instance;

    private HttpHelper () {
        /*SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(
        new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));*/
        cm = new PoolingClientConnectionManager();
        cm.setMaxTotal(10);
        cm.setDefaultMaxPerRoute(10); 
        connEvictor = new IdleConnectionEvictor(cm);
        connEvictor.start();
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 15000);
        httpclient = new DefaultHttpClient(cm, httpParams);
    }


    public static HttpHelper getInstance() {
        if (instance == null) instance = new HttpHelper();
        return instance;
    }

    public synchronized void initMultiThreadedMode() {
        log.debug("initMultiThreadedMode");
        if(cm != null) cm.shutdown();
        cm = new PoolingClientConnectionManager();
        cm.setMaxTotal(200);
        cm.setDefaultMaxPerRoute(200); 
        connEvictor = new IdleConnectionEvictor(cm);
        connEvictor.start();
        final HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 40000);
        httpclient = new DefaultHttpClient(cm, httpParams);
    }
    
    public void shutdown () {
        try { 
            httpclient.getConnectionManager().shutdown(); 
            if(connEvictor != null) {
                connEvictor.shutdown();
                connEvictor.join();
            }
        } 
        catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }
    
    public ResponseVS getData (String serverURL, String contentType) throws IOException, ParseException {
        log.debug("getData - serverURL: " + serverURL + " - contentType: " + contentType);
        ResponseVS responseVS = null;
        HttpResponse response = null;
		HttpGet httpget = null;
        try {
			httpget = new HttpGet(serverURL);
            if(contentType != null) httpget.setHeader("Content-Type", contentType);
            response = httpclient.execute(httpget);
            log.debug("----------------------------------------");
            /*Header[] headers = response.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {
            System.out.println(headers[i]);
            }*/
            log.debug(response.getStatusLine().toString());
            log.debug("----------------------------------------");
            if(ResponseVS.SC_OK == response.getStatusLine().getStatusCode()) {
                byte[] responseBytes = EntityUtils.toByteArray(response.getEntity());
                responseVS = new ResponseVS(response.getStatusLine().getStatusCode(),  responseBytes);
            } else {
                responseVS = new ResponseVS(response.getStatusLine().getStatusCode(),
                        EntityUtils.toString(response.getEntity()));
            }
        } catch(Exception ex) {
            log.error(ex.getMessage(), ex);
            responseVS = new ResponseVS(ResponseVS.SC_ERROR, ApplicationContextHolder.getSimulationContext().getMessage(
                    "hostConnectionErrorMsg", serverURL));
        } finally {
            if(response != null) EntityUtils.consume(response.getEntity());
            if(httpget != null) httpget.abort();
            return responseVS;
        }
    }
    
    public X509Certificate getCertFromServer (String serverURL) throws Exception {
        log.debug("getCertFromServer - serverURL: " + serverURL);
        HttpGet httpget = new HttpGet(serverURL);
        X509Certificate certificate = null;
        HttpResponse response = httpclient.execute(httpget);
        log.debug("----------------------------------------");
        log.debug(response.getStatusLine().toString());
        log.debug("----------------------------------------");
        HttpEntity entity = response.getEntity();
        if (ResponseVS.SC_OK == response.getStatusLine().getStatusCode()) {
            certificate = CertUtil.fromPEMToX509Cert(EntityUtils.toByteArray(entity));
        }
        EntityUtils.consume(response.getEntity());
        return certificate;
    }
    
    public Collection<X509Certificate> getCertChainHTTP (
            String serverURL) throws Exception {
        log.debug("getCertChainHTTP - serverURL: " + serverURL);
        HttpGet httpget = new HttpGet(serverURL);
        Collection<X509Certificate> certificates = null;
        log.debug("getCertFromServer - lanzando: " + httpget.getURI());
        HttpResponse response = httpclient.execute(httpget);
        log.debug("----------------------------------------");
        log.debug(response.getStatusLine().toString());
        log.debug("----------------------------------------");
        HttpEntity entity = response.getEntity();
        if (ResponseVS.SC_OK == response.getStatusLine().getStatusCode()) {
            certificates = CertUtil.fromPEMToX509CertCollection(EntityUtils.toByteArray(entity));
        }
        EntityUtils.consume(response.getEntity());
        return certificates;
    }
   
    public PKIXParameters getPKIXParametersHTTP (String certChainURL) throws Exception {
        log.debug("getPKIXParametersHTTP - certChainURL: " + certChainURL);
        Set<TrustAnchor> anchors = getTrustAnchorHTTP(certChainURL);
        PKIXParameters params = new PKIXParameters(anchors);
        params.setRevocationEnabled(false); // tell system do not check CRL's
        return params;       
    }   
    
    public Set<TrustAnchor> getTrustAnchorHTTP (String certChainURL) throws Exception {
        log.debug("getTrustAnchorHTTP - certChainURL: " + certChainURL);
        Collection<X509Certificate> certificates = getCertChainHTTP(certChainURL);
        Set<TrustAnchor> anchors = new HashSet<TrustAnchor>();
        for (X509Certificate certificate:certificates) {
            TrustAnchor certAnchor = new TrustAnchor(certificate, null);
            anchors.add(certAnchor);
        }
        return anchors;
    }

    
    public ResponseVS sendFile (File file, String contentType, String serverURL, 
            String... headerNames) {
        log.debug("sendFile - contentType: " + contentType + 
                " - serverURL: " + serverURL); 
        ResponseVS responseVS = null;
        HttpPost httpPost = null;
        try {
			httpPost = new HttpPost(serverURL);
            FileEntity entity = new FileEntity(file, ContentType.create(contentType));
            httpPost.setEntity(entity);
            HttpResponse response = httpclient.execute(httpPost);
            log.debug("----------------------------------------");
            log.debug(response.getStatusLine().toString());
            log.debug("----------------------------------------"); 
            byte[] responseBytes =  EntityUtils.toByteArray(response.getEntity());
            responseVS = new ResponseVS(response.getStatusLine().getStatusCode(), responseBytes);
            if(headerNames != null) {
                List<String> headerValues = new ArrayList<String>();
                for(String headerName: headerNames) {
                    org.apache.http.Header headerValue = response.getFirstHeader(headerName);
                    headerValues.add(headerValue.getValue());
                }
                responseVS.setData(headerValues);
            }
            EntityUtils.consume(response.getEntity());
        } catch(Exception ex) {
            log.error(ex.getMessage(), ex);
            responseVS = new ResponseVS(ResponseVS.SC_ERROR, ex.getMessage());
            if(httpPost != null) httpPost.abort();
        }
        return responseVS;
    }
        
        
    public ResponseVS sendString (String stringToSend, 
            String paramName, String serverURL) {
        log.debug("sendString - serverURL: " + serverURL);
        ResponseVS responseVS = null;
        HttpPost httpPost = null;
        try {
			httpPost = new HttpPost(serverURL);
            StringBody stringBody = new StringBody(stringToSend);
            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.STRICT);
            reqEntity.addPart(paramName, stringBody);
            httpPost.setEntity(reqEntity);
            HttpResponse response = httpclient.execute(httpPost);
            log.debug("----------------------------------------");
            log.debug(response.getStatusLine().toString());
            log.debug("----------------------------------------");
        } catch(Exception ex) {
            log.error(ex.getMessage(), ex);
            responseVS = new ResponseVS(ResponseVS.SC_ERROR, ex.getMessage());
            if(httpPost != null) httpPost.abort();
        }
        return responseVS;
    }
    
    public ResponseVS sendByteArray(byte[] byteArray, String contentType,
            String serverURL, String... headerNames) throws IOException {
        log.debug("sendByteArray - contentType: " + contentType + 
                " - serverURL: " + serverURL);
        ResponseVS responseVS = null;
        HttpPost httpPost = null;
        HttpResponse response = null;
        try {
			httpPost = new HttpPost(serverURL);
            ByteArrayEntity entity = null;
            if(contentType != null) {
                entity = new ByteArrayEntity(byteArray,  ContentType.create(contentType));
            } else entity = new ByteArrayEntity(byteArray); 
            httpPost.setEntity(entity);
            response = httpclient.execute(httpPost);
            log.debug("----------------------------------------");
            log.debug(response.getStatusLine().toString());
            log.debug("----------------------------------------");
            byte[] responseBytes = EntityUtils.toByteArray(response.getEntity());
            responseVS = new ResponseVS(response.getStatusLine().getStatusCode(), responseBytes);
            if(headerNames != null) {
                List<String> headerValues = new ArrayList<String>();
                for(String headerName: headerNames) {
                    org.apache.http.Header headerValue = response.getFirstHeader(headerName);
					if(headerValue != null) {
						headerValues.add(headerValue.getValue());
					} else log.error(" - headerName '" + headerName + "' without response!!!");
                }
                responseVS.setData(headerValues);
            }
            EntityUtils.consume(response.getEntity());
        } catch(HttpHostConnectException ex){
            log.error(ex.getMessage(), ex);
            responseVS = new ResponseVS(ResponseVS.SC_ERROR, ApplicationContextHolder.getSimulationContext().getMessage(
                    "hostConnectionErrorMsg", serverURL));
            if(httpPost != null)  httpPost.abort();
        } catch(Exception ex) {
            log.error(ex.getMessage(), ex);
            responseVS = new ResponseVS(ResponseVS.SC_ERROR, ex.getMessage());
            if(httpPost != null) httpPost.abort();
        }  finally {
            if(response != null) EntityUtils.consume(response.getEntity());
            return responseVS;
        }
    }
    
    
    public ResponseVS sendObjectMap(Map<String, Object> fileMap, String serverURL) throws Exception {
        log.debug("sendObjectMap - serverURL: " + serverURL); 
        ResponseVS responseVS = null;
        if(fileMap == null || fileMap.isEmpty()) throw new Exception(ApplicationContextHolder.getSimulationContext().
                getMessage("requestWithoutFileMapErrorMsg"));
        HttpPost httpPost = null;
        HttpResponse response = null;
        try {
			httpPost = new HttpPost(serverURL);
            Set<String> fileNames = fileMap.keySet();
            MultipartEntity reqEntity = new MultipartEntity();
            for(String objectName: fileNames){
                Object objectToSend = fileMap.get(objectName);
                if(objectToSend instanceof File) {
                    File file = (File)objectToSend;
                    log.debug("sendObjectMap - fileName: " + objectName + " - filePath: " + file.getAbsolutePath());
                    FileBody  fileBody = new FileBody(file);
                    reqEntity.addPart(objectName, fileBody);
                } else if (objectToSend instanceof byte[]) {
                    log.debug("sendObjectMap - byteArray - fileName: " + objectName);
                    byte[] byteArray = (byte[])objectToSend;
                    reqEntity.addPart(objectName, new ByteArrayBody(byteArray, objectName));
                } else log.error("sendObjectMap - unknown object type: " + objectToSend.getClass().getName());
            }
            httpPost.setEntity(reqEntity);
            response = httpclient.execute(httpPost);     
            log.debug("----------------------------------------");
            log.debug(response.getStatusLine().toString());
            log.debug("----------------------------------------");
            byte[] responseBytes =  EntityUtils.toByteArray(response.getEntity());
            responseVS = new ResponseVS(response.getStatusLine().getStatusCode(), responseBytes);
            //EntityUtils.consume(response.getEntity());
        } catch(Exception ex) {
            String statusLine = null;
            if(response != null) {
                statusLine = response.getStatusLine().toString();
            }
            log.error(ex.getMessage() + " - StatusLine: " + statusLine, ex);
            responseVS = new ResponseVS(ResponseVS.SC_ERROR, ex.getMessage());
            if(httpPost != null) httpPost.abort();
        }
        return responseVS;  
    }


    public static class IdleConnectionEvictor extends Thread {

        private final ClientConnectionManager connMgr;

        private volatile boolean shutdown;

        public IdleConnectionEvictor(ClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
        }

        @Override
        public void run() {
            try {
                while (!shutdown) {
                    synchronized (this) {
                        wait(3000);
                        // Close expired connections
                        connMgr.closeExpiredConnections();
                        // Optionally, close connections
                        // that have been idle longer than 30 sec
                        connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                    }
                }
            } catch (InterruptedException ex) {
                log.error(ex.getMessage(), ex);
            }
        }

        public void shutdown() {
            shutdown = true;
            synchronized (this) {
                notifyAll();
            }
        }
    }
        
    public static void main(String[] args) throws IOException, Exception { }
    
}