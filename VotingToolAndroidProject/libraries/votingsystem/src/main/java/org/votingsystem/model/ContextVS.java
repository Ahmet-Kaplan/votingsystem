package org.votingsystem.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.itextpdf.text.Context_iTextVS;

import org.votingsystem.signature.util.VotingSystemKeyGenerator;

import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ContextVS {

    public enum State {WITH_CERTIFICATE, WITH_CSR, WITHOUT_CSR}

    public static final String TAG = "ContextVS";

    public static final String OCSP_DNIE_URL = "http://ocsp.dnie.es";

    public static final int VOTE_TAG                                = 0;
    public static final int REPRESENTATIVE_VOTE_TAG                 = 1;
    public static final int ANONYMOUS_REPRESENTATIVE_DELEGATION_TAG = 2;

    public static final String VOTING_SYSTEM_BASE_OID = "0.0.0.0.0.0.0.0.0.";
    public static final String REPRESENTATIVE_VOTE_OID = VOTING_SYSTEM_BASE_OID + REPRESENTATIVE_VOTE_TAG;
    public static final String ANONYMOUS_REPRESENTATIVE_DELEGATION_OID = VOTING_SYSTEM_BASE_OID +
            ANONYMOUS_REPRESENTATIVE_DELEGATION_TAG;
    public static final String VOTE_OID = VOTING_SYSTEM_BASE_OID + VOTE_TAG;

    public static final String STATE_KEY                  = "state";
    public static final String CSR_REQUEST_ID_KEY         = "csrRequestId";
    public static final String APPLICATION_ID_KEY         = "idAplicacion";
    public static final String EVENT_KEY                  = "eventKey";
    public static final String SIGNED_FILE_NAME           = "signedFile";
    public static final String CSR_FILE_NAME              = "csr";
    public static final String ACCESS_REQUEST_FILE_NAME   = "accessRequest";
    public static final String DEFAULT_SIGNED_FILE_NAME   = "smimeMessage.p7m";
    public static final String PROVIDER                   = "BC";
    public static final String SERVER_URL_EXTRA_PROP_NAME = "serverURL";

    public static final int KEY_SIZE = 1024;
    public static final int EVENTS_PAGE_SIZE = 30;
    public static final int MAX_SUBJECT_SIZE = 60;
    public static final int SELECTED_OPTION_MAX_LENGTH       = 60;
    //TODO por el bug en froyo de -> JcaDigestCalculatorProviderBuilder
    public static final String SIG_HASH = "SHA256";
    public static final String SIG_NAME = "RSA";
    /** Random Number Generator algorithm. */
    private static final String ALGORITHM_RNG = "SHA1PRNG";
    public static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";
    //public static final String VOTE_SIGN_MECHANISM = "SHA512withRSA";
    public static final String VOTE_SIGN_MECHANISM = "SHA256WithRSA";
    public static final String USER_CERT_ALIAS = "CertificadoUsuario";
    public static final String KEY_STORE_FILE = "keyStoreFile.p12";

    public static final String TIMESTAMP_USU_HASH = "2.16.840.1.101.3.4.2.1";//TSPAlgorithms.SHA256
    public static final String TIMESTAMP_VOTE_HASH = "2.16.840.1.101.3.4.2.1";//TSPAlgorithms.SHA256

    public static final String ASUNTO_MENSAJE_FIRMA_DOCUMENTO = "[Firma]-";
    public static final String VOTING_HEADER_LABEL  = "votingSystemMessageType";

    public static final String CERT_NOT_FOUND_DIALOG_ID      = "certNotFoundDialog";

    private State state = State.WITHOUT_CSR;
    private List<SubSystemChangeListener> subSystemChangeListeners = new ArrayList<SubSystemChangeListener>();
    private SubSystemVS selectedSubsystem = SubSystemVS.VOTING;
    private EventVSState navigationDrawerEventState = EventVSState.OPEN;
    private EventVS eventVSSeleccionado;
    private ArrayList<EventVS> eventsSelectedList;

    private AccessControlVS accessControlVS;
    private UserVS userVS;
    private Map<String, X509Certificate> certsMap = new HashMap<String, X509Certificate>();
    private OperationVS operationVS = null;

    private static ContextVS INSTANCE;
    private Context context = null;

    private ExecutorService executorService;

    private static PropertyResourceBundle resourceBundle;

    private ContextVS(Context context) {
        System.setProperty("android.os.Build.ID", android.os.Build.ID);
        this.context = context.getApplicationContext();
        try {
            Context_iTextVS.init(context);
            VotingSystemKeyGenerator.INSTANCE.init(SIG_NAME, PROVIDER, KEY_SIZE, ALGORITHM_RNG);
            InputStream inputStream = context.getAssets().open("messages_es.properties");
            resourceBundle = new PropertyResourceBundle(inputStream);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public static ContextVS getInstance(Context context) {
        if(INSTANCE == null) {
            Log.d(TAG + ".getInstance(...)", "getInstance -  android.os.Build.ID: " +
                    android.os.Build.ID);
            INSTANCE = new ContextVS(context);
        }
        return INSTANCE;
    }

    public void setEvent(EventVS event) {
        Log.d(TAG + ".setEvent(...)", "- eventId: " + event.getId() + " - type: " + event.getTypeVS());
        this.eventVSSeleccionado = event;
    }

    public List<EventVS> getEvents() {
        return eventsSelectedList;
    }

    public void setEventList(List<EventVS> events) {
        Log.d(TAG + ".setEventList(...)", " --- setEventList - events.size(): " + events.size());
        eventsSelectedList = new ArrayList<EventVS>();
        if(events != null) {
            for(EventVS event: events) {
                ((EventVS)event).setAccessControlVS(accessControlVS);
                eventsSelectedList.add(event);
            }
        }
    }

    public static String getMessage(String key, Object... arguments) {
        try {
            String pattern = resourceBundle.getString(key);
            if(arguments.length > 0) return MessageFormat.format(pattern, arguments);
            else return resourceBundle.getString(key);
        } catch(Exception ex) {
            ex.printStackTrace();
            Log.d(TAG + "getMessage(...)", "### Value not found for key: " + key);
            return "---" + key + "---";
        }
    }


    public String getHostID() {
        return android.os.Build.ID;
    }

    public int getEventIndex(EventVS event) {
        return eventsSelectedList.indexOf(event);
    }

    public EventVS getEvent() {
        return eventVSSeleccionado;
    }

    public Future<ResponseVS> submit(Callable<ResponseVS> callable) {
        Log.d(TAG + ".submit(...)", " --- submit");
        if(executorService == null) executorService = Executors.newFixedThreadPool(3);
        return executorService.submit(callable);
    }

    public OperationVS getOperationVS() {
        return operationVS;
    }

    public void setOperationVS(OperationVS operationVS) {
        if(operationVS == null) Log.d(TAG + ".setOperationVS(...)", "- removing pending operationVS");
        else Log.d(TAG + ".setOperationVS(...)", "- operationVS: " + operationVS.getTypeVS());
        this.operationVS = operationVS;
    }

    public void setState(State state) {
        Log.d(TAG + ".setState(...)", " - state: " + state.toString()
                + " - server: " + STATE_KEY + "_" + accessControlVS.getServerURL());
        this.state = state;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(STATE_KEY + "_" + accessControlVS.getServerURL() , state.toString());
        editor.commit();
    }

    public State getState() {
        return state;
    }

    public EventVSState getNavigationDrawerEventState() {
        return navigationDrawerEventState;
    }

    public void setNavigationDrawerEventState(EventVSState eventState) {
        this.navigationDrawerEventState = eventState;
    }


    public SubSystemVS getSelectedSubsystem () {
        return selectedSubsystem;
    }

    public X509Certificate getCert(String serverURL) {
        Log.d(TAG + ".getCert(...)", " - getCert - serverURL: " + serverURL);
        if(serverURL == null) return null;
        return certsMap.get(serverURL);
    }

    public void putCert(String serverURL, X509Certificate cert) {
        Log.d(TAG + ".putCert(...)", " - putCert - serverURL: " + serverURL);
        certsMap.put(serverURL, cert);
    }

    public UserVS getUserVS() {
        return userVS;
    }

    public void setUserVS(UserVS userVS) {
        this.userVS = userVS;
    }

    public AccessControlVS getAccessControl() {
        return accessControlVS;
    }

    public void setAccessControlVS(AccessControlVS accessControlVS) {
        Log.d(TAG + ".setAccessControlURL() ", " - setAccessControlURL: " +
                accessControlVS.getServerURL());
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        String stateStr = settings.getString(
                STATE_KEY + "_" + accessControlVS.getServerURL(), State.WITHOUT_CSR.toString());
        state = State.valueOf(stateStr);
        this.accessControlVS = accessControlVS;
    }

    public void addSubSystemChangeListener(SubSystemChangeListener listener) {
        subSystemChangeListeners.add(listener);
    }

    public void removeSubSystemChangeListener(SubSystemChangeListener listener) {
        subSystemChangeListeners.remove(listener);
    }

    public void setSelectedSubsystem (SubSystemVS selectedSubsystem) {
        Log.d(TAG + ".setSelectedSubsystem(...)", " - Subsystem: " + selectedSubsystem);
        this.selectedSubsystem = selectedSubsystem;
        for(SubSystemChangeListener listener : subSystemChangeListeners) {
            listener.onChangeSubSystem(selectedSubsystem);
        }
    }

}