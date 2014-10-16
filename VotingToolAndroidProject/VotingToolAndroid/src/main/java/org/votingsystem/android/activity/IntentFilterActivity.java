package org.votingsystem.android.activity;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.bouncycastle2.util.encoders.Base64;
import org.json.JSONObject;
import org.votingsystem.android.AppContextVS;
import org.votingsystem.android.R;
import org.votingsystem.android.fragment.MessageDialogFragment;
import org.votingsystem.android.service.BootStrapService;
import org.votingsystem.android.util.PrefUtils;
import org.votingsystem.model.ContextVS;
import org.votingsystem.model.EventVS;
import org.votingsystem.model.OperationVS;
import org.votingsystem.model.ResponseVS;
import org.votingsystem.model.TypeVS;
import org.votingsystem.util.HttpHelper;

import java.util.Properties;


/**
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class IntentFilterActivity extends FragmentActivity {
	
	public static final String TAG = IntentFilterActivity.class.getSimpleName();

    private ProgressDialog progressDialog = null;
    private OperationVS operationVS;
    private AppContextVS contextVS;
    private String broadCastId = IntentFilterActivity.class.getSimpleName();

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            Log.d(TAG + ".broadcastReceiver.onReceive(...)", "extras: " + intent.getExtras());
            ResponseVS responseVS = intent.getParcelableExtra(ContextVS.RESPONSEVS_KEY);
            try {
                if(operationVS != null  && ContextVS.State.WITH_CERTIFICATE == contextVS.getState()){
                    if(operationVS.getEventVS() != null) {
                        //We don't pass all eventvs data on uri because content can be very large
                        responseVS = HttpHelper.getData(operationVS.getEventVS().getURL(), null);
                        if(ResponseVS.SC_OK == responseVS.getStatusCode()) {
                            EventVS selectedEvent = EventVS.parse(new JSONObject(responseVS.getMessage()));
                            Log.d(TAG + ".onStartCommand(...)", " _ TODO _ Fetch option selected");
                            operationVS.setEventVS(selectedEvent);
                        }
                    } else if(operationVS.getTypeVS() == TypeVS.TRANSACTIONVS ||
                            operationVS.getTypeVS() == TypeVS.FROM_USERVS) {
                        Intent newIntent = new Intent(getBaseContext(), VicketPagerActivity.class);
                        newIntent.putExtra(ContextVS.OPERATIONVS_KEY, operationVS);
                        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(newIntent);
                    }
                } else {
                    Intent responseIntent = null;
                    switch (contextVS.getState()) {
                        case WITHOUT_CSR:
                            String applicationID = PrefUtils.getApplicationId(contextVS);
                            Log.d(TAG + ".processOperation(.. ) ", "WITHOUT_CSR - applicationID: " +
                                    applicationID);
                            responseIntent = new Intent(getBaseContext(), CertRequestActivity.class);
                            break;
                        case WITH_CSR:
                            responseIntent = new Intent(getBaseContext(), CertResponseActivity.class);
                            break;
                        case WITH_CERTIFICATE:
                            responseIntent = new Intent(getBaseContext(), EventsVSActivity.class);
                            break;
                    }
                    if(intent != null) {
                        responseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(responseIntent);
                    }
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    };


    @Override protected void onCreate(Bundle savedInstanceState) {
        //boolean isTablet = getResources().getBoolean(R.bool.isTablet); this doesn't work
        Log.d(TAG + ".onCreate(...)", "savedInstanceState: " + savedInstanceState);
    	super.onCreate(savedInstanceState);
        contextVS = (AppContextVS) getApplicationContext();
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            String query = getIntent().getStringExtra(SearchManager.QUERY);
            Log.d(TAG + ".onCreate()", "Intent.ACTION_SEARCH - query: " + query);
            return;
        }
        showProgressDialog(getString(R.string.connecting_caption),
                getString(R.string.loading_data_msg));
        AppContextVS contextVS = (AppContextVS) getApplicationContext();

        ResponseVS responseVS = getIntent().getParcelableExtra(ContextVS.RESPONSEVS_KEY);
        if(responseVS != null) {
            if(progressDialog != null) progressDialog.dismiss();
            showMessage(responseVS.getStatusCode(), responseVS.getCaption(), responseVS.getMessage());
            return;
        }

        Uri uriData = getIntent().getData();
        if(uriData != null) {
            try {
                Properties props = new Properties();
                props.load(getAssets().open("VotingSystem.properties"));
                String vicketServerURL = props.getProperty(ContextVS.VICKET_SERVER_URL);
                String accessControlURL = props.getProperty(ContextVS.ACCESS_CONTROL_URL_KEY);
                Intent startIntent = new Intent(getApplicationContext(), BootStrapService.class);
                startIntent.putExtra(ContextVS.URI_KEY, uriData);
                startIntent.putExtra(ContextVS.ACCESS_CONTROL_URL_KEY, accessControlURL);
                startIntent.putExtra(ContextVS.VICKET_SERVER_URL, vicketServerURL);
                startIntent.putExtra(ContextVS.CALLER_KEY, broadCastId);
                startService(startIntent);
                operationVS = null;
                if(uriData != null) {
                    String operationStr = uriData.getQueryParameter("operation");
                    if(operationStr != null) {
                        operationVS = new OperationVS(TypeVS.valueOf(operationStr), uriData);
                    } else {
                        String encodedMsg = uriData.getQueryParameter("operationvs");
                        if(encodedMsg != null) {
                            byte[] decodedMsgBytes = Base64.decode(encodedMsg.getBytes());
                            operationVS = OperationVS.parse(new String(decodedMsgBytes, "UTF-8"));
                            //operationVS = OperationVS.parse(StringUtils.decodeString(encodedMsg));
                        }
                    }
                }

            } catch(Exception ex) {ex.printStackTrace();}
        } else {
            Intent intent = new Intent(this, EventsVSActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void showProgressDialog(String title, String dialogMessage) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(true);
        progressDialog.setTitle(title);
        progressDialog.setMessage(dialogMessage);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    private void showMessage(Integer statusCode, String caption, String message) {
        Log.d(TAG + ".showMessage(...) ", "statusCode: " + statusCode + " - caption: " + caption +
                " - message: " + message);
        MessageDialogFragment newFragment = MessageDialogFragment.newInstance(statusCode, caption,
                message);
        newFragment.show(getSupportFragmentManager(), MessageDialogFragment.TAG);
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG + ".onDestroy()", "onDestroy");
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
    };

    @Override public void onResume() {
        Log.d(TAG + ".onResume() ", "");
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(broadCastId));
    }

    @Override public void onPause() {
        Log.d(TAG + ".onPause(...)", "");
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

}