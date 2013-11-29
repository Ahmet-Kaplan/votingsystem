package org.votingsystem.model;

import android.util.Log;
import org.json.JSONObject;
import org.votingsystem.signature.smime.SMIMEMessageWrapper;
import org.votingsystem.signature.smime.SignedMailValidator;

import java.security.cert.PKIXParameters;
import java.util.Date;


/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
*/
public class ResponseVS<T> {
    
    public static final int SC_OK = 200;
    public static final int SC_OK_ANULACION_ACCESS_REQUEST = 270;
    public static final int SC_ERROR_REQUEST = 400;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_ERROR_VOTE_REPEATED = 470;
    public static final int SC_CANCELLATION_REPEATED = 471;
    public static final int SC_NULL_REQUEST = 472;

    public static final int SC_ERROR           = 500;
    public static final int SC_ERROR_EXCEPCION = 500;
    public static final int SC_ERROR_TIMESTAMP = 570;    
    public static final int SC_PROCESSING      = 700;
    public static final int SC_CANCELLED       = 0;
    public static final int SC_INITIALIZED     = 1;
    public static final int SC_PAUSED          = 10;


    private int statusCode;
    private String status;
    private EventVSResponse eventQueryResponse;
    private String message;
    private T data;
    private TypeVS typeVS;
    private Date fecha;
    private Long eventId;
    private SMIMEMessageWrapper smimeMessage;
    private ActorVS actorVS;
    private byte[] messageBytes;
    
    
    public ResponseVS(int statusCode,
                      String message, byte[] messageBytes) {
        this.statusCode = statusCode;
        this.message = message;
        this.messageBytes = messageBytes;
    }

    public ResponseVS(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
    
    public ResponseVS(int statusCode, byte[] messageBytes) {
        this.statusCode = statusCode;
        this.messageBytes = messageBytes;
    }
    
    public ResponseVS(int statusCode) {
        this.statusCode = statusCode;
    }
    
    public ResponseVS(int statusCode, SMIMEMessageWrapper recibo, PKIXParameters params) throws Exception {
        this.statusCode = statusCode;
        this.setSmimeMessage(recibo);
        SignedMailValidator.ValidationResult validationResult = recibo.verify(params);        
        if (validationResult.isValidSignature()) {
            JSONObject resultadoJSON = new JSONObject(recibo.getSignedContent());
            Log.d("", " Receipt OK");
         } else Log.e("", " Receipt with errors");
    }

    /*
    public ResponseVS(int statusCode, SMIMEMessageWrapper recibo) throws Exception {
        this.statusCode = statusCode;
        this.setSmimeMessage(recibo);
        if (recibo.isValidSignature()) {
            JSONObject resultadoJSON = new JSONObject(recibo.getSignedContent());
            Log.e("ResponseVS", "ResultadoJSON: " + resultadoJSON.toString());
            if (resultadoJSON.has("tipoRespuesta")) 
                typeVS = TypeVS.valueOf(resultadoJSON.getString("tipoRespuesta"));
            if (resultadoJSON.has("fecha"))  
                fecha = DateUtils.getDateFromString(resultadoJSON.getString("fecha"));
            if (resultadoJSON.has("message"))
                message = resultadoJSON.getString("message");
         } else Log.e("ResponseVS","Error en la validación de la respuesta");
    }*/
    
    
    public ResponseVS() {  }
    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
    
    public String toString () {
    	StringBuilder respuesta = new StringBuilder();
    	respuesta.append("State: ").append(statusCode).append(" - Mensaje: ").append(message);
    	if (typeVS != null) {
    		respuesta.append(" - TypeVS: ").append(typeVS.toString());
    	}
    	return respuesta.toString();
    }

    /**
     * @return the eventQueryResponse
     */
    public EventVSResponse getEventQueryResponse() {
        return eventQueryResponse;
    }

    /**
     * @param eventQueryResponse the eventQueryResponse to set
     */
    public void setEventQueryResponse(EventVSResponse eventQueryResponse) {
        this.eventQueryResponse = eventQueryResponse;
    }

    /**
     * @return the statusCodeHTTP
     */
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public TypeVS getTypeVS() {
        return typeVS;
    }

    public void setTypeVS(TypeVS typeVS) {
        this.typeVS = typeVS;
    }

    public void setFecha(Date fecha) {
            this.fecha = fecha;
    }

    public Date getFecha() {
            return fecha;
    }

    public Long getEventVSId() {
        return eventId;
    }

    public void setEventVSId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * @return the actorVS
     */
    public ActorVS getActorVS() {
        return actorVS;
    }

    /**
     * @param actorVS the actorVS to set
     */
    public void setActorVS(ActorVS actorVS) {
        this.actorVS = actorVS;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

	public byte[] getMessageBytes() {
		return messageBytes;
	}

	public void setMessageBytes(byte[] messageBytes) {
		this.messageBytes = messageBytes;
	}

	public SMIMEMessageWrapper getSmimeMessage() {
		return smimeMessage;
	}

	public void setSmimeMessage(SMIMEMessageWrapper smimeMessage) {
		this.smimeMessage = smimeMessage;
	}

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
