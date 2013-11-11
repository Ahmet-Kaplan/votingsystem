/*
 * Copyright 2011 - Jose. J. García Zornoza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.votingsystem.android;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

//import org.eclipse.jetty.websocket.WebSocket;
//import org.eclipse.jetty.websocket.WebSocketClient;
//import org.eclipse.jetty.websocket.WebSocketClientFactory;
import org.votingsystem.android.model.AccessControl;
import org.votingsystem.android.model.ContextVSAndroid;
import org.votingsystem.android.model.EventVSAndroid;
import org.votingsystem.callable.DataGetter;
import org.votingsystem.android.model.OperationVSAndroid;
import org.votingsystem.model.ResponseVS;
import org.votingsystem.android.util.ServerPaths;
import org.votingsystem.util.StringUtils;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import static org.votingsystem.android.model.ContextVSAndroid.PREFS_ID_APLICACION;
import static org.votingsystem.android.model.ContextVSAndroid.SERVER_URL_EXTRA_PROP_NAME;

public class MainActivity extends FragmentActivity {
	
	public static final String TAG = "MainActivity";

    private ContextVSAndroid contextVSAndroid;
    private ProgressDialog progressDialog = null;
    private OperationVSAndroid operationVSAndroid = null;
    private Uri uriData = null;
    private String accessControlURL = null;

    @Override protected void onCreate(Bundle savedInstanceState) {
        //boolean isTablet = getResources().getBoolean(R.bool.isTablet); this doesn't work
        Log.i(TAG + ".onCreate(...)", " - onCreate  ");
    	super.onCreate(savedInstanceState);  
        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {
            String query = getIntent().getStringExtra(SearchManager.QUERY);
            Log.d(TAG + ".onCreate()", " - Intent.ACTION_SEARCH - query: "+ query);
            return;
        }
        contextVSAndroid = ContextVSAndroid.getInstance(getBaseContext());

        if(Intent.ACTION_VIEW.equals(getIntent().getAction())) {
        	//getIntent().getCategories().contains(Intent.CATEGORY_BROWSABLE);
            uriData = getIntent().getData();
            accessControlURL = uriData.getQueryParameter("serverURL");
        } else if(getIntent().getStringExtra(SERVER_URL_EXTRA_PROP_NAME) != null) {
            accessControlURL = getIntent().getStringExtra(SERVER_URL_EXTRA_PROP_NAME);
        } else {
            Properties props = new Properties();
            try {
                props.load(getAssets().open("VotingSystem.properties"));
                accessControlURL = props.getProperty("CONTROL_ACCESO_URL");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        AccessControlLoader accessControlLoader = new AccessControlLoader();
        accessControlLoader.execute(accessControlURL);
        //WebsocketLoader websocketLoader = new WebsocketLoader();
        //websocketLoader.execute("ws://192.168.1.20:8080/SistemaVotacionTest/websocket/service");
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG +  ".onCreateOptionsMenu(..)", " - onCreateOptionsMenu");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG + ".onOptionsItemSelected(...) ", " - item: " + item.getTitle());
        switch (item.getItemId()) {
            case R.id.reload:
                if(accessControlURL == null) {
                    try {
                        Properties props = new Properties();
                        props.load(getAssets().open("VotingSystem.properties"));
                        accessControlURL = props.getProperty("CONTROL_ACCESO_URL");
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                AccessControlLoader accessControlLoader = new AccessControlLoader();
                accessControlLoader.execute(accessControlURL);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override public void onResume() {
    	super.onResume();
    	Log.d(TAG + ".onResume() ", " - onResume");
    }

    private void showProgressDialog(String title, String dialogMessage) {
        if (progressDialog == null)
            progressDialog = new ProgressDialog(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(title);
        progressDialog.setMessage(dialogMessage);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void setActivityState(ContextVSAndroid.Estado estado) {
    	Log.d(TAG + ".setActivityState()", " - estado: " + estado);
    	Intent intent = null;
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        contextVSAndroid.setEstado(estado);
    	switch (estado) {
	    	case SIN_CSR:
	    		String idAplicacion = settings.getString(PREFS_ID_APLICACION, null);
	    		if (idAplicacion == null || "".equals(idAplicacion)) {
	    			Log.d(TAG + ".setActivityState() ", " - guardando ID aplicación");
	    			idAplicacion = UUID.randomUUID().toString();
	    			SharedPreferences.Editor editor = settings.edit();
	    			editor.putString(PREFS_ID_APLICACION, idAplicacion);
			        editor.commit();
	    		}
	            setContentView(R.layout.main_activity);
	            Button cancelarButton = (Button) findViewById(R.id.cancelar_button);
	            cancelarButton.setOnClickListener(new OnClickListener() {
	                public void onClick(View v) { 
	                	//finish(); 
	                	Intent intent = new Intent(getBaseContext(), NavigationDrawer.class);
	                	startActivity(intent);
	                }
	            });
	            
	            Button solicitarButton = (Button) findViewById(R.id.solicitar_button);
	            solicitarButton.setOnClickListener(new OnClickListener() {
	                public void onClick(View v) {
	                	Intent intent = new Intent(getBaseContext(), UserCertRequestActivity.class);
	                	startActivity(intent);
	                }
	            });
	    		break;
	    	case CON_CSR:
	    		intent = new Intent(getBaseContext(), UserCertResponseActivity.class);
	    		break;
	    	case CON_CERTIFICADO:
	    		intent = new Intent(getBaseContext(), NavigationDrawer.class);
	    		break;
    	}
    	if(intent != null) {
    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	startActivity(intent);	
    	}
    }
    
    private void processOperation(OperationVSAndroid operationVSAndroid, ContextVSAndroid.Estado estado) {
    	Log.d(TAG + ".processOperation(...)", "- operationVSAndroid: " +
    			operationVSAndroid.getTipo() + " - estado: " + estado);
        contextVSAndroid.setEvent(operationVSAndroid.getEventVSAndroid());
        Intent intent = null;
        if(ContextVSAndroid.Estado.CON_CERTIFICADO == estado) {
    		switch(operationVSAndroid.getTipo()) {
		        case ENVIO_VOTO_SMIME:
                    intent = new Intent(MainActivity.this, VotingEventFragment.class);
                    break;
		        case FIRMA_MANIFIESTO_PDF:
		        case FIRMA_RECLAMACION_SMIME:
                    intent = new Intent(MainActivity.this, EventFragment.class);
		        	break;
		        default: 
		        	Log.e(TAG + ".processOperation(...)", "- unknown operationVSAndroid");;
	        }
            if(intent != null) {
                try {
                    intent.putExtra(ContextVSAndroid.EVENT_KEY, operationVSAndroid.getEventVSAndroid().toJSON().toString());
                    startActivity(intent);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
    	} else {
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle(getString(R.string.cert_not_found_caption)).
    			setMessage(R.string.cert_not_found_msg).show();
    		setActivityState(estado);
    	}
    }

    private void showMessage(String caption, String message) {
        Log.d(TAG + ".showMessage(...) ", " - caption: "
                + caption + "  - showMessage: " + message);
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setTitle(caption).setMessage(message).show();
    }

    @Override protected void onStop() {
        super.onStop();
    	Log.d(TAG + ".onStop()", " - onStop");
    };

    @Override protected void onDestroy() {
        super.onDestroy();
    	Log.d(TAG + ".onDestroy()", " - onDestroy");
    };

    /*public class WebsocketLoader extends AsyncTask<String, String, ResponseVS> {

        CountDownLatch countDownLatch = new CountDownLatch(1);

        public WebsocketLoader() { }

        @Override protected void onPreExecute() {
            Log.d(TAG + ".WebsocketLoader.onPreExecute() ", " - onPreExecute");
        }

        @Override protected ResponseVS doInBackground(String... urls) {
            String websocketURL = urls[0];
            Log.d(TAG + ".WebsocketLoader.doInBackground() ", " - websocketURL: " + websocketURL);
            // Bridge Jetty Logging to Android Logging
            //System.setProperty("org.eclipse.jetty.util.log.class",AndroidLog.class.getName());
            //org.eclipse.jetty.util.log.Log.setLog(new AndroidLog());

            WebSocketClientFactory factory = new WebSocketClientFactory();
            try {
                factory.start();
                WebSocketClient client = factory.newWebSocketClient();
                Log.d(TAG +  "WebSocketClient", " - getMaxTextMessageSize: " + client.getMaxTextMessageSize());


                WebSocket.Connection connection = client.open(new URI(websocketURL), new WebSocket.OnTextMessage() {
                    public void onOpen(Connection connection) {
                        Log.d(TAG +  "WebSocketClient.onOpen(..)", " - onOpen");
                    }

                    public void onClose(int closeCode, String message) {
                        Log.d(TAG +  "WebSocketClient.onClose(..)", " - onClose");
                        countDownLatch.countDown();
                    }

                    public void onMessage(String data) {
                        Log.d(TAG +  "WebSocketClient.onMessage(..)", " - onMessage - data: " + data);
                        publishProgress(data);
                    }
                }).get(300, TimeUnit.SECONDS);
                countDownLatch.await();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
            return new ResponseVS(ResponseVS.SC_OK);
        }

        @Override  protected void onProgressUpdate(String... progress) {
            Log.d(TAG +  "WebSocketClient.onProgressUpdate(..)", " - data: " + progress[0]);

        }

        @Override  protected void onPostExecute(ResponseVS responseVS) {
            Log.d(TAG + ".EventInfoLoader.onPostExecute() ", " - statusCode: " + responseVS.getStatusCode());
        }
    }*/



    public class EventInfoLoader extends AsyncTask<String, Void, ResponseVS> {

        public EventInfoLoader() { }

        @Override protected void onPreExecute() {
            showProgressDialog(getString(R.string.connecting_caption),
                    getString(R.string.loading_data_msg));
        }

        @Override protected ResponseVS doInBackground(String... urls) {
            String eventURL = urls[0];
            Log.d(TAG + ".EventInfoLoader.doInBackground() ", " - eventURL: " + eventURL);
            try {
                DataGetter dataGetter = new DataGetter(null, eventURL);
                return dataGetter.call();
            } catch(Exception ex) {
                ex.printStackTrace();
                return new ResponseVS(ResponseVS.SC_ERROR, ex.getMessage());
            }
        }

        @Override  protected void onPostExecute(ResponseVS responseVS) {
            Log.d(TAG + ".EventInfoLoader.onPostExecute() ", " - statusCode: " + responseVS.getStatusCode());
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            try {
                if(ResponseVS.SC_OK == responseVS.getStatusCode()) {
                    EventVSAndroid selectedEvent = EventVSAndroid.parse(responseVS.getMessage());
                    selectedEvent.setOpcionSeleccionada(operationVSAndroid.
                            getEventVSAndroid().getOpcionSeleccionada());
                    operationVSAndroid.setEventVSAndroid(selectedEvent);
                    contextVSAndroid.setEvent(selectedEvent);
                    processOperation(operationVSAndroid, contextVSAndroid.getEstado());
                } else showMessage(getString(R.string.error_lbl), responseVS.getMessage());
            } catch(Exception ex) {
                ex.printStackTrace();
                showMessage(getString(R.string.error_lbl), ex.getMessage());
            }
        }
    }


    public class AccessControlLoader extends AsyncTask<String, Void, ResponseVS> {

        private String serviceURL = null;

        public AccessControlLoader() { }

        @Override protected void onPreExecute() {
            showProgressDialog(getString(R.string.connecting_caption),
                    getString(R.string.loading_data_msg));
        }

        @Override protected ResponseVS doInBackground(String... urls) {
            Log.d(TAG + ".AccessControlLoader.doInBackground() ", " - serviceURL: " + urls[0]);
            serviceURL = urls[0];
            try {
                DataGetter dataGetter = new DataGetter(null, ServerPaths.getURLInfoServidor(urls[0]));
                return dataGetter.call();
            } catch(Exception ex) {
                ex.printStackTrace();
                return new ResponseVS(ResponseVS.SC_ERROR, ex.getMessage());
            }
        }

        @Override  protected void onPostExecute(ResponseVS responseVS) {
            Log.d(TAG + ".AccessControlLoader.onPostExecute() ", " - statusCode: " + responseVS.getStatusCode());
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            try {
                if(ResponseVS.SC_OK == responseVS.getStatusCode()) {
                    AccessControl accessControl = AccessControl.parse(responseVS.getMessage());
                    contextVSAndroid.setAccessControlURL(serviceURL);
                    contextVSAndroid.setAccessControl(accessControl);
                    if(uriData == null) {
                        setActivityState(contextVSAndroid.getEstado());
                    } else {//loaded from web browser session
                        String encodedMsg = uriData.getQueryParameter("msg");
                        String msg = StringUtils.decodeString(encodedMsg);
                        Log.d(TAG + ".onPostExecute() - ", " - launched by browser - host: " +
                                uriData.getHost() + " - path: " + uriData.getPath() +
                                " - userInfo: " + uriData.getUserInfo() +
                                " - msg: " + msg);
                        if(msg != null) {
                            operationVSAndroid = OperationVSAndroid.parse(msg);
                        } else {
                            Log.d(TAG + ".onPostExecute(...)", "- msg null");
                            operationVSAndroid = new OperationVSAndroid();
                        }
                        if(operationVSAndroid.getEventVSAndroid() != null) {
                            EventInfoLoader getDataTask = new EventInfoLoader();
                            getDataTask.execute(operationVSAndroid.getEventVSAndroid().getURL());
                        } else {
                            Log.d(TAG + ".onPostExecute(...)", " - operationVSAndroid: " + operationVSAndroid.getTipo());
                            if(msg != null) {
                                Intent intent = new Intent(MainActivity.this, EventPublishingActivity.class);
                                intent.putExtra(OperationVSAndroid.OPERATION_KEY, msg);
                                startActivity(intent);
                            }
                        }
                    }
                } else showMessage(getString(R.string.error_lbl), responseVS.getMessage());
            } catch(Exception ex) {
                ex.printStackTrace();
                showMessage(getString(R.string.error_lbl), ex.getMessage());
            }
        }
    }
}