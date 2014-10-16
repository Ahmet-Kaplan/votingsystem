package org.votingsystem.model;

import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.log4j.Logger;
import org.votingsystem.signature.smime.SMIMEMessage;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
* @author jgzornoza
* Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
*/
public class ResponseVS<T> implements Serializable {

    public static final long serialVersionUID = 1L;
    
	private static Logger log = Logger.getLogger(ResponseVS.class);
    
    public static final int SC_OK                       = 200;
    public static final int SC_OK_WITHOUT_BODY          = 204;
    public static final int SC_OK_CANCEL_ACCESS_REQUEST = 270;
    public static final int SC_MESSAGE_FROM_VS          = 277;
    public static final int SC_REQUEST_TIMEOUT          = 408;
    public static final int SC_ERROR_REQUEST            = 400;
    public static final int SC_NOT_FOUND                = 404;
    public static final int SC_PRECONDITION_FAILED      = 412;
    public static final int SC_ERROR_REQUEST_REPEATED   = 409;
    public static final int SC_EXCEPTION                = 490;
    public static final int SC_NULL_REQUEST             = 472;
    public static final int SC_ERROR                    = 500;
    public static final int SC_CONNECTION_TIMEOUT       = 522;
    public static final int SC_ERROR_TIMESTAMP          = 570;
    public static final int SC_PROCESSING               = 700;
    public static final int SC_TERMINATED               = 710;
    public static final int SC_CANCELLED                = 0;
    public static final int SC_INITIALIZED              = 1;
    public static final int SC_PAUSED                   = 10;
    
    private Integer statusCode;
    private StatusVS<?> status;
    private String message;
    private JSON messageJSON;
    private String reason;
    private String metaInf;
    private String url;
    private SMIMEMessage smimeMessage;
    private EventVS eventVS;
    private T data;
    private TypeVS type;
    private UserVS userVS;
    private byte[] messageBytes;
    private ContentTypeVS contentType = ContentTypeVS.TEXT;
    private File file;
    private List<String> errorList;
    private Integer size;
        
    public ResponseVS () {  }

    public ResponseVS (int statusCode) {
        this.statusCode = statusCode;    
    }

    public ResponseVS (int statusCode, ContentTypeVS contentType) {
        this.statusCode = statusCode;
        this.contentType = contentType;
    }

    public ResponseVS (int statusCode, String msg) {
        this.statusCode = statusCode;
        this.message = msg;
    }

    public ResponseVS (int statusCode, String msg, T data) {
        this.statusCode = statusCode; 
        this.message = msg;
        this.data = data;
    }

    public ResponseVS (int statusCode, String msg, ContentTypeVS contentType) {
        this.statusCode = statusCode;
        this.message = msg;
        this.contentType = contentType;
    }

    public ResponseVS (int statusCode, StatusVS<?> status, String msg) {
        this.statusCode = statusCode;
        this.status = status;
        this.message = msg;
    }

    public ResponseVS (int statusCode, StatusVS<?> status) {
        this.statusCode = statusCode;
        this.status = status;
    }

    public ResponseVS (int statusCode, byte[] messageBytes) {
        this.statusCode = statusCode;
        this.messageBytes = messageBytes;
    }
        
    public ResponseVS (int statusCode, byte[] messageBytes, ContentTypeVS contentType) {
        this.statusCode = statusCode;
        this.messageBytes = messageBytes;
        this.contentType = contentType;
    }

    public String getMessage() {
        if(message == null && messageBytes != null) {
            try {
                message = new String(messageBytes, "UTF-8");
            } catch(Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return message;
    }

    public String getAlertMessage() {
        return ((metaInf ==null)?" ":metaInf) + "###" + ((message == null)?" ":message) + "###" +
                ((reason == null)?" ":reason);
    }

    public JSON getMessageJSON() {
        if(messageJSON != null) return messageJSON;
        String message = getMessage();
        if(message != null) return JSONSerializer.toJSON(message);
        return null;
    }

    public void setMessageJSON(JSON jsonObject) {
        this.messageJSON = jsonObject;
    }

    public JSON getSignedJSON() {
        if(smimeMessage == null) return null;
        else return JSONSerializer.toJSON(smimeMessage.getSignedContent());
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public String toString () {
    	StringBuilder responseVS = new StringBuilder();
    	responseVS.append("statusCode: ").append(statusCode).append(" - Message: ").append(message);
    	if (type != null) {
    		responseVS.append(" - Type: ").append(type.toString());
    	}
    	return responseVS.toString();
    }

    public int getStatusCode() {
        return statusCode; 
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public TypeVS getType() {
        return type;
    }

    public void setType(TypeVS type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void appendMessage(String msg) {
        if(message != null) message = message + "\n" + msg;
        else message = msg;
    }

    public List<String> getErrorList() {
        return errorList;
    }

    public void setErrorList(List<String> errorList) {
        this.errorList = errorList;
    }

    public byte[] getMessageBytes() {
        if(messageBytes == null && message != null) return message.getBytes();
        return messageBytes;
    }

    public void setMessageBytes(byte[] messageBytes) {
        this.messageBytes = messageBytes;
    }

    public SMIMEMessage getSmimeMessage() {
        return smimeMessage;
    }

    public void setSmimeMessage(SMIMEMessage smimeMessage) {
        this.smimeMessage = smimeMessage;
    }

    public EventVS getEventVS() {
        return eventVS;
    }

    public void setEventVS(EventVS eventVS) {
        this.eventVS = eventVS;
    }

	public UserVS getUserVS() {
		return userVS;
	}

	public ResponseVS setUserVS(UserVS userVS) {
		this.userVS = userVS;
        return this;
	}

    public <E> StatusVS<E> getStatus() {
        return (StatusVS<E>)status;
    }

    public <E> void setStatus(StatusVS<E> status) {
        this.status = status;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ContentTypeVS getContentType() {
        return contentType;
    }

    public void setContentType(ContentTypeVS contentType) {
        this.contentType = contentType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getMetaInf() {
        return metaInf;
    }

    public void setMetaInf(String metaInf) {
        this.metaInf = metaInf;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static ResponseVS parseWebSocketResponse(String message) {
        JSONObject messageJSON = (JSONObject)JSONSerializer.toJSON(message);
        ResponseVS result = new ResponseVS();
        result.setMessageJSON(messageJSON);
        if(messageJSON.containsKey("operation")) result.setType(TypeVS.valueOf(messageJSON.getString("operation")));
        if(messageJSON.containsKey("status")) result.setStatusCode(Integer.valueOf(messageJSON.getString("status")));
        if(messageJSON.containsKey("message")) result.setMessage(messageJSON.getString("message"));
        if(messageJSON.containsKey("URL")) result.setMessage(messageJSON.getString("URL"));
        return result;
    }

}