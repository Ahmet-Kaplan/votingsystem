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
import java.util.Collection;
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

    public static final String STATE_KEY                   = "STATE";
    public static final String CSR_REQUEST_ID_KEY          = "csrRequestId";
    public static final String APPLICATION_ID_KEY          = "idAplicacion";
    public static final String EVENT_KEY                   = "eventKey";
    public static final String VOTING_SYSTEM_PRIVATE_PREFS = "VotingSystemSharedPrivatePreferences";



    public static final String SIGNED_FILE_NAME           = "signedFile";
    public static final String CSR_FILE_NAME              = "csr";
    public static final String ACCESS_REQUEST_FILE_NAME   = "accessRequest";
    public static final String DEFAULT_SIGNED_FILE_NAME   = "smimeMessage.p7m";
    public static final String PROVIDER                   = "BC";
    public static final String SERVER_URL_EXTRA_PROP_NAME = "serverURL";

    //Intent keys
    public static final String FRAGMENT_KEY = "FRAGMENT_KEY";
    public static final String PIN_KEY = "PIN";
    public static final String URL_KEY = "URL";
    public static final String URI_DATA_KEY = "URI_DATA";
    public static final String ACCESS_CONTROL_URL_KEY = "ACCESS_CONTROL_URL";
    public static final String SERVICE_CALLER_KEY = "SERVICE_CALLER";
    public static final String HTTP_RESPONSE_DATA_KEY = "HTTP_RESPONSE_DATA";
    public static final String HTTP_RESPONSE_STATUS_KEY = "HTTP_RESPONSE_STATUS";
    public static final String OFFSET_KEY = "OFFSET";
    public static final String CAPTION_KEY = "CAPTION";
    public static final String MESSAGE_KEY = "MESSAGE";
    public static final String LOADING_KEY = "LOADING_KEY";
    public static final String NUM_TOTAL_KEY = "NUM_TOTAL";
    public static final String LIST_STATE_KEY = "LIST_STATE";
    public static final String ITEM_ID_KEY = "ITEM_ID";
    public static final String CURSOR_POSITION_KEY = "CURSOR_POSITION";
    public static final String EVENT_STATE_KEY = "EVENT_STATE";
    public static final String EVENT_TYPE_KEY  = "EVENT_TYPE";
    public static final String EVENTVS_KEY  = "EVENTVS";

    //Actions IDs
    public static final String SIGN_AND_SEND_ACTION_ID = "SIGN_AND_SEND_ACTION";
    public static final String PIN_ACTION_ID = "PIN_ACTION_ID";
    public static final String HTTP_DATA_INITIALIZED_ACTION_ID = "HTTP_DATA_INITIALIZED_ACTION";

    //Loaders
    public static final int EVENT_LIST_LOADER1_ID = 0;
    public static final int VOTING_EVENT_ACTIVE_LIST_LOADER_ID = 2;
    public static final int VOTING_EVENT_PENDING_LIST_LOADER_ID = 3;
    public static final int VOTING_EVENT_TERMINATED_LIST_LOADER_ID = 4;
    public static final int REPRESENTATIVE_LOADER_ID = 1;

    //Pages size
    //public static final Integer REPRESENTATIVE_PAGE_SIZE = 100;
    public static final Integer REPRESENTATIVE_PAGE_SIZE = 20;
    public static final Integer EVENTVS_PAGE_SIZE = 20;

    //Notifications IDs
    public static final int RSS_SERVICE_NOTIFICATION_ID           = 1;
    public static final int SIGN_AND_SEND_SERVICE_NOTIFICATION_ID = 2;

    public static final int KEY_SIZE = 1024;
    public static final int SYMETRIC_ENCRYPTION_KEY_LENGTH = 256;
    public static final int SYMETRIC_ENCRYPTION_ITERATION_COUNT = 100;

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

    public static final String VOTING_HEADER_LABEL  = "votingSystemMessageType";
    public static final String CERT_NOT_FOUND_DIALOG_ID      = "certNotFoundDialog";

    private State state = State.WITHOUT_CSR;

    private AccessControlVS accessControlVS;
    private UserVS userVS;
    private List<UserVS> representativeList;
    private Map<String, X509Certificate> certsMap = new HashMap<String, X509Certificate>();
    private OperationVS operationVS = null;

    private static ContextVS INSTANCE;
    private Context context = null;

    private ExecutorService executorService;

    private static PropertyResourceBundle resourceBundle;

    private ContextVS(Context context) {
        System.setProperty("android.os.Build.ID", android.os.Build.ID);
        this.context = context;
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

    public static String getMessage(String key, Object... arguments) {
        try {
            String pattern = resourceBundle.getString(key);
            if(arguments != null && arguments.length > 0)
                return MessageFormat.format(pattern, arguments);
            else return pattern;
        } catch(Exception ex) {
            ex.printStackTrace();
            Log.d(TAG + "getMessage(...)", "### Value not found for key: " + key);
            return "---" + key + "---";
        }
    }

    public List<UserVS> getRepresentatives(int offset, int size) {
        if(representativeList == null) return null;
        return representativeList.subList(offset, size);
    }

    public void setRepresentatives(int offset, Collection<UserVS> representativeSet) {
        if(representativeList == null) {
            int initialCapacity = offset + representativeSet.size();
            representativeList = new ArrayList<UserVS>(initialCapacity);
            representativeList.addAll(offset, representativeSet);
        } else representativeList.addAll(offset, representativeSet);
    }

    public String getHostID() {
        return android.os.Build.ID;
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
        SharedPreferences settings = context.getSharedPreferences(
                VOTING_SYSTEM_PRIVATE_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(STATE_KEY + "_" + accessControlVS.getServerURL() , state.toString());
        editor.commit();
    }

    public State getState() {
        return state;
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
        SharedPreferences settings = context.getSharedPreferences(
                VOTING_SYSTEM_PRIVATE_PREFS, Context.MODE_PRIVATE);
        String stateStr = settings.getString(
                STATE_KEY + "_" + accessControlVS.getServerURL(), State.WITHOUT_CSR.toString());
        state = State.valueOf(stateStr);
        this.accessControlVS = accessControlVS;
    }

}