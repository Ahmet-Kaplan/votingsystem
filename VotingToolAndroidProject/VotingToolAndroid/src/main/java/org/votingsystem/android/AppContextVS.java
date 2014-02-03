package org.votingsystem.android;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;

import com.itextpdf.text.Context_iTextVS;

import org.votingsystem.android.activity.MessageActivity;
import org.votingsystem.android.callable.MessageTimeStamper;
import org.votingsystem.model.AccessControlVS;
import org.votingsystem.model.ContextVS;
import org.votingsystem.model.ControlCenterVS;
import org.votingsystem.model.OperationVS;
import org.votingsystem.model.ResponseVS;
import org.votingsystem.model.TicketServer;
import org.votingsystem.model.UserVS;
import org.votingsystem.signature.smime.SMIMEMessageWrapper;
import org.votingsystem.signature.smime.SignedMailGenerator;
import org.votingsystem.signature.util.KeyStoreUtil;
import org.votingsystem.signature.util.VotingSystemKeyGenerator;
import org.votingsystem.util.DateUtils;
import org.votingsystem.util.FileUtils;
import org.votingsystem.util.ObjectUtils;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.votingsystem.model.ContextVS.ALGORITHM_RNG;
import static org.votingsystem.model.ContextVS.KEY_SIZE;
import static org.votingsystem.model.ContextVS.KEY_STORE_FILE;
import static org.votingsystem.model.ContextVS.NIF_KEY;
import static org.votingsystem.model.ContextVS.PROVIDER;
import static org.votingsystem.model.ContextVS.SIGNATURE_ALGORITHM;
import static org.votingsystem.model.ContextVS.SIG_NAME;
import static org.votingsystem.model.ContextVS.STATE_KEY;
import static org.votingsystem.model.ContextVS.State;
import static org.votingsystem.model.ContextVS.USER_CERT_ALIAS;
import static org.votingsystem.model.ContextVS.USER_DATA_FILE_NAME;
import static org.votingsystem.model.ContextVS.VOTING_SYSTEM_PRIVATE_PREFS;

/**
 * @author jgzornoza
 * Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
 */
public class AppContextVS extends Application {

    public static final String TAG = "AppContextVS";

    private State state = State.WITHOUT_CSR;
    private String ticketServerURL;
    private AccessControlVS accessControl;
    private ControlCenterVS controlCenter;
    private TicketServer ticketServer;
    private UserVS userVS;
    private Map<String, X509Certificate> certsMap = new HashMap<String, X509Certificate>();
    private OperationVS operationVS = null;


	@Override public void onCreate() {
        //System.setProperty("android.os.Build.ID", android.os.Build.ID);
        Log.d(TAG + ".onCreate()", "");
        try {
            Context_iTextVS.init(getApplicationContext());
            VotingSystemKeyGenerator.INSTANCE.init(SIG_NAME, PROVIDER, KEY_SIZE, ALGORITHM_RNG);
            Properties props = new Properties();
            props.load(getAssets().open("VotingSystem.properties"));
            ticketServerURL = props.getProperty(ContextVS.TICKET_SERVER_URL);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
	}

    public String getTicketServerURL() {
        return ticketServerURL;
    }

    public String getHostID() {
        return android.os.Build.ID;
    }

    public OperationVS getOperationVS() {
        return operationVS;
    }

    public void setOperationVS(OperationVS operationVS) {
        if(operationVS == null) Log.d(TAG + ".setOperationVS(...)", "- removing pending operationVS");
        else Log.d(TAG + ".setOperationVS(...)", "- operationVS: " + operationVS.getTypeVS());
        this.operationVS = operationVS;
    }

    public void setState(State state, String nif) {
        Log.d(TAG + ".setState(...)", STATE_KEY + "_" + accessControl.getServerURL() +
                " - state: " + state.toString());
        SharedPreferences settings = getSharedPreferences(
                VOTING_SYSTEM_PRIVATE_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(STATE_KEY + "_" + accessControl.getServerURL() , state.toString());
        if(nif != null) editor.putString(NIF_KEY, nif);
        if(State.WITH_CERTIFICATE == state) loadUser();
        editor.commit();
        this.state = state;
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
        Log.d(TAG + ".putCert(...)", " serverURL: " + serverURL);
        certsMap.put(serverURL, cert);
    }

    public X509Certificate getTimeStampCert() {
        if(accessControl == null) return null;
        return getAccessControl().getTimeStampCert();
    }

    public UserVS getUserVS() {
        return userVS;
    }

    public AccessControlVS getAccessControl() {
        return accessControl;
    }

    public void setAccessControlVS(AccessControlVS accessControl) {
        Log.d(TAG + ".setAccessControlURL() ", " - setAccessControlURL: " +
                accessControl.getServerURL());
        SharedPreferences settings = getSharedPreferences(
                VOTING_SYSTEM_PRIVATE_PREFS, Context.MODE_PRIVATE);
        String stateStr = settings.getString(
                STATE_KEY + "_" + accessControl.getServerURL(), State.WITHOUT_CSR.toString());
        state = State.valueOf(stateStr);
        this.accessControl = accessControl;
        loadUser();
    }

    public void loadUser() {
        try {
            File representativeDataFile = new File(getFilesDir(), USER_DATA_FILE_NAME);
            if(representativeDataFile.exists()) {
                byte[] serializedUserData = FileUtils.getBytesFromFile(
                        representativeDataFile);
                userVS = (UserVS) ObjectUtils.deSerializeObject(serializedUserData);
            } else Log.d(TAG + ".setAccessControlVS(...)", "user data not found");
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getLapseWeekLbl(Calendar calendar) {
        Calendar thisWeekMonday = DateUtils.getMonday(calendar);
        calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        return getString(R.string.week_lapse_lbl, DateUtils.getDate_Es(
                thisWeekMonday.getTime()), DateUtils.getDate_Es(calendar.getTime()));
    }

    public void updateTicketAccountLastChecked() {
        SharedPreferences settings = getSharedPreferences(
                VOTING_SYSTEM_PRIVATE_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(ContextVS.TICKET_ACCOUNT_LAST_CHECKED_KEY,
        Calendar.getInstance().getTimeInMillis());
        editor.commit();
    }

    public Date getTicketAccountLastChecked() {
        SharedPreferences pref = getSharedPreferences(ContextVS.VOTING_SYSTEM_PRIVATE_PREFS,
                Context.MODE_PRIVATE);
        GregorianCalendar lastCheckedTime = new GregorianCalendar();
        lastCheckedTime.setTimeInMillis(pref.getLong(ContextVS.TICKET_ACCOUNT_LAST_CHECKED_KEY, 0));

        Calendar currentLapseCalendar = DateUtils.getMonday(Calendar.getInstance());

        if(lastCheckedTime.getTime().after(currentLapseCalendar.getTime())) {
            return lastCheckedTime.getTime();
        } else return null;
    }

    public String getCurrentWeekLapseId() {
        Calendar currentLapseCalendar = DateUtils.getMonday(Calendar.getInstance());
        return DateUtils.getDirPath(currentLapseCalendar.getTime());
    }

    public ResponseVS signMessage(String toUser, String textToSign, String subject, String pin) {
        ResponseVS responseVS = null;
        try {
            FileInputStream fis = openFileInput(KEY_STORE_FILE);
            byte[] keyStoreBytes = FileUtils.getBytesFromInputStream(fis);
            String userVS = getUserVS().getNif();
            Log.d(TAG + ".signMessage(...) ", "subject: " + subject);
            SignedMailGenerator signedMailGenerator = new SignedMailGenerator(
                    keyStoreBytes, USER_CERT_ALIAS, pin.toCharArray(), SIGNATURE_ALGORITHM);
            SMIMEMessageWrapper smimeMessage = signedMailGenerator.genMimeMessage(userVS, toUser,
                    textToSign, subject);
            MessageTimeStamper timeStamper = new MessageTimeStamper(smimeMessage,
                    (AppContextVS)getApplicationContext());
            responseVS = timeStamper.call();
            if(ResponseVS.SC_OK != responseVS.getStatusCode()) {
                responseVS.setCaption(getString(R.string.timestamp_service_error_caption));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            String message = ex.getMessage();
            if(message == null || message.isEmpty()) message = getString(R.string.exception_lbl);
            responseVS = ResponseVS.getExceptionResponse(getString(R.string.exception_lbl),message);
        } finally {
            return responseVS;
        }
    }

    public boolean checkPin(String pin) {
        try {
            FileInputStream fis = openFileInput(KEY_STORE_FILE);
            byte[] keyStoreBytes = FileUtils.getBytesFromInputStream(fis);
            KeyStore keyStore = KeyStoreUtil.getKeyStoreFromBytes(keyStoreBytes, pin.toCharArray());
            return true;
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public void setControlCenter(ControlCenterVS controlCenter) {
        this.controlCenter = controlCenter;
    }

    public ControlCenterVS getControlCenter() {
        return controlCenter;
    }

    public TicketServer getTicketServer() {
        return ticketServer;
    }

    public void setTicketServer(TicketServer ticketServer) {
        this.ticketServer = ticketServer;
    }


    public void showNotification(ResponseVS responseVS){
        NotificationManager notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        Intent clickIntent = new Intent(this, MessageActivity.class);
        clickIntent.putExtra(ContextVS.RESPONSEVS_KEY, responseVS);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, ContextVS.
                TICKET_SERVICE_NOTIFICATION_ID, clickIntent, PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(responseVS.getCaption()).setContentText(Html.fromHtml(
                        responseVS.getNotificationMessage())).setSmallIcon(responseVS.getIconId())
                .setContentIntent(pendingIntent);
        Notification note = builder.build();
        // hide the notification after its selected
        note.flags |= Notification.FLAG_AUTO_CANCEL;
        //Identifies our service icon in the icon tray.
        notificationManager.notify(ContextVS.REPRESENTATIVE_SERVICE_NOTIFICATION_ID, note);
    }

    public void sendBroadcast(ResponseVS responseVS) {
        Log.d(TAG + ".sendMessage(...) ", "statusCode: " + responseVS.getStatusCode() +
                " - type: " + responseVS.getTypeVS() + " - serviceCaller: " +
                responseVS.getServiceCaller());
        Intent intent = new Intent(responseVS.getServiceCaller());
        intent.putExtra(ContextVS.RESPONSEVS_KEY, responseVS);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
