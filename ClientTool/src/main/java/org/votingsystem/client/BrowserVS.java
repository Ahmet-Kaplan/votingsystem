package org.votingsystem.client;

import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.sf.json.JSON;
import netscape.javascript.JSObject;
import org.apache.log4j.Logger;
import org.controlsfx.glyphfont.FontAwesome;
import org.votingsystem.client.dialog.InboxDialog;
import org.votingsystem.client.dialog.MessageDialog;
import org.votingsystem.client.dialog.PasswordDialog;
import org.votingsystem.client.pane.BrowserVSPane;
import org.votingsystem.client.service.WebSocketService;
import org.votingsystem.client.service.WebSocketServiceAuthenticated;
import org.votingsystem.client.util.*;
import org.votingsystem.model.ContentTypeVS;
import org.votingsystem.model.ContextVS;
import org.votingsystem.model.OperationVS;
import org.votingsystem.model.ResponseVS;
import org.votingsystem.signature.util.CryptoTokenVS;
import org.votingsystem.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class BrowserVS extends Region implements WebKitHost, WebSocketListener {

    private static Logger log = Logger.getLogger(BrowserVS.class);
    private static final int BROWSER_WIDTH = 1200;
    private static final int BROWSER_HEIGHT = 1000;
    private static final int MAX_CHARACTERS_TAB_CAPTION = 25;

    private Stage browserStage;
    private Map<String, WebView> webViewMap = new HashMap<String, WebView>();
    private TextField locationField = new TextField("");
    private final BrowserVSPane browserHelper;
    private HBox toolBar;
    private TabPane tabPane;
    private Button prevButton;
    private Button messageToDeviceButton;
    private WebSocketServiceAuthenticated webSocketServiceAuthenticated;
    private WebSocketService webSocketService;
    private PasswordDialog webSocketMessagePasswordDialog;
    private static final BrowserVS INSTANCE = new BrowserVS();

    public static BrowserVS getInstance() {
        return INSTANCE;
    }

    private BrowserVS() {
        browserHelper = new BrowserVSPane();
        Platform.setImplicitExit(false);
        browserHelper.getSignatureService().setOnSucceeded(event -> {
            log.debug("signatureService - OnSucceeded");
            PlatformImpl.runLater(() -> {
                ResponseVS responseVS = browserHelper.getSignatureService().getValue();
                if(responseVS.getStatus() != null) {
                    NotificationManager.getInstance().postToEventBus(responseVS);
                } else if(ResponseVS.SC_INITIALIZED == responseVS.getStatusCode()) {
                    log.debug("signatureService - OnSucceeded - ResponseVS.SC_INITIALIZED");
                } else if(ContentTypeVS.JSON == responseVS.getContentType()) {
                    sendMessageToBrowser(responseVS.getMessageJSON(),
                            browserHelper.getSignatureService().getOperationVS().getCallerCallback());
                } else sendMessageToBrowser(Utils.getMessageToBrowser(responseVS.getStatusCode(),
                        responseVS.getMessage()), browserHelper.getSignatureService().getOperationVS().
                        getCallerCallback());
            });
        });
        browserHelper.getSignatureService().setOnRunning(event -> log.debug("signatureService - OnRunning"));
        browserHelper.getSignatureService().setOnCancelled(event -> log.debug("signatureService - OnCancelled"));
        browserHelper.getSignatureService().setOnFailed(event -> log.debug("signatureService - OnFailed"));
        initComponents();
    }

    private void initComponents() {
        log.debug("initComponents");
        browserStage = new Stage();
        browserStage.initModality(Modality.WINDOW_MODAL);
        browserStage.setTitle(ContextVS.getMessage("mainDialogCaption"));
        browserStage.setResizable(true);
        browserStage.setOnCloseRequest(event -> {
            event.consume();
            browserStage.hide();
            browserHelper.getSignatureService().cancel();
            log.debug("browserStage.setOnCloseRequest");
        });
        VBox mainVBox = new VBox();
        prevButton = new Button();
        final Button forwardButton = new Button();
        final Button reloadButton = new Button();
        forwardButton.setGraphic(Utils.getImage(FontAwesome.Glyph.CHEVRON_RIGHT));
        messageToDeviceButton = new Button();
        messageToDeviceButton.setGraphic(Utils.getImage(FontAwesome.Glyph.ENVELOPE, Utils.COLOR_RED_DARK));
        messageToDeviceButton.setOnAction((event) -> {
            consumeMessageTodDevice(ContextVS.getMessage("inboxPinDialogMsg"));
        });
        forwardButton.setOnAction((event) -> {
            try {
                ((WebView)tabPane.getSelectionModel().getSelectedItem().getContent()).getEngine().getHistory().go(1);
                prevButton.setDisable(false);
            } catch(Exception ex) { forwardButton.setDisable(true); }
        });
        prevButton.setGraphic(Utils.getImage(FontAwesome.Glyph.CHEVRON_LEFT));
        prevButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent ev) {
                try {
                    ((WebView) tabPane.getSelectionModel().getSelectedItem().getContent()).getEngine().getHistory().go(-1);
                    forwardButton.setDisable(false);
                } catch (Exception ex) {
                    prevButton.setDisable(true);
                }
            }
        });
        reloadButton.setGraphic(Utils.getImage(FontAwesome.Glyph.REFRESH));
        reloadButton.setOnAction(event -> ((WebView) tabPane.getSelectionModel().getSelectedItem().getContent()).
                getEngine().load(locationField.getText()));
        prevButton.setDisable(true);
        forwardButton.setDisable(true);
        locationField.setPrefWidth(400);
        HBox.setHgrow(locationField, Priority.ALWAYS);
        locationField.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                if (!"".equals(locationField.getText())) {
                    String targetURL = null;
                    if (locationField.getText().startsWith("http://") || locationField.getText().startsWith("https://")) {
                        targetURL = locationField.getText().trim();
                    } else targetURL = "http://" + locationField.getText().trim();
                    ((WebView) tabPane.getSelectionModel().getSelectedItem().getContent()).getEngine().load(targetURL);
                }
            }});
        toolBar = new HBox();
        toolBar.setAlignment(Pos.CENTER);
        toolBar.getStyleClass().add("browser-toolbar");
        NotificationManager.getInstance().setAlertButton(new Button());
        toolBar.getChildren().addAll(prevButton, forwardButton, locationField, reloadButton, Utils.createSpacer(),
                NotificationManager.getInstance().getAlertButton(), messageToDeviceButton);
        tabPane = new TabPane();
        tabPane.setRotateGraphic(false);
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.SELECTED_TAB);
        tabPane.setSide(Side.TOP);
        HBox.setHgrow(tabPane, Priority.ALWAYS);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        final AnchorPane tabPainContainer = new AnchorPane();
        final Button addButton = new Button("+");
        addButton.getStyleClass().add("newtab-button");
        AnchorPane.setTopAnchor(tabPane, 0.0);
        AnchorPane.setLeftAnchor(tabPane, 0.0);
        AnchorPane.setRightAnchor(tabPane, 0.0);
        AnchorPane.setBottomAnchor(tabPane, 0.0);
        AnchorPane.setTopAnchor(addButton, 1.0);
        AnchorPane.setLeftAnchor(addButton, 5.0);
        addButton.setOnAction(event -> newTab(null, null, null));
        tabPainContainer.getChildren().addAll(tabPane, addButton);
        VBox.setVgrow(tabPainContainer, Priority.ALWAYS);
        mainVBox.getChildren().addAll(toolBar, tabPainContainer);
        mainVBox.getStylesheets().add(((Object)this).getClass().getResource("/css/browservs.css").toExternalForm());
        browserHelper.getChildren().add(0, mainVBox);
        browserStage.setScene(new Scene(browserHelper));
        browserStage.setWidth(BROWSER_WIDTH);
        browserStage.setHeight(BROWSER_HEIGHT);
    }

    public WebView newTab(String URL, String tabCaption, String jsCommand) {
        final WebView webView = new WebView();
        if(jsCommand != null) {
            webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newState) -> {
                    //log.debug("newState: " + newState);
                    if (newState == Worker.State.SUCCEEDED) webView.getEngine().executeScript(jsCommand.toString());
                });
        }
        final WebHistory history = webView.getEngine().getHistory();
        history.getEntries().addListener(new ListChangeListener<WebHistory.Entry>(){
            @Override public void onChanged(Change<? extends WebHistory.Entry> c) {
                c.next();
                if(history.getCurrentIndex() > 0) prevButton.setDisable(false);
                //log.debug("==== currentIndex: " + history.getCurrentIndex() + " - num. entries: " + history.getEntries().size());
                String params = "";
                if(locationField.getText().contains("?")) {
                    params = locationField.getText().substring(locationField.getText().indexOf("?"),
                            locationField.getText().length());
                }
                WebHistory.Entry selectedEntry = history.getEntries().get(history.getEntries().size() - 1);
                log.debug("history change - selectedEntry: " + selectedEntry);
                String newURL = selectedEntry.getUrl();
                if(!newURL.contains("?")) newURL = newURL + params;
                if(history.getEntries().size() > 1 && selectedEntry.getTitle() != null) tabPane.getSelectionModel().
                        getSelectedItem().setText(selectedEntry.getTitle());
                locationField.setText(newURL);
            }
        });
        webView.getEngine().setUserDataDirectory(new File(ContextVS.WEBVIEWDIR));
        webView.getEngine().locationProperty().addListener((observable, oldValue, newValue) ->
                locationField.setText(newValue));
        webView.getEngine().setCreatePopupHandler(config -> {//handle popup windows
            //WebView newView = new WebView();
            //newView.setFontScale(0.8);
            //new BrowserVS(newView).show(700, 700, false);
            return newTab(null, null, null).getEngine();
        });
        webView.getEngine().getLoadWorker().stateProperty().addListener(
            new ChangeListener<Worker.State>() {
                @Override public void changed(ObservableValue<? extends Worker.State> ov,
                                    Worker.State oldState, Worker.State newState) {
                    //log.debug("newState: " + newState + " - " + webView.getEngine().getLocation());
                    if (newState == Worker.State.SUCCEEDED) {
                        Document doc = webView.getEngine().getDocument();
                        Element element = doc.getElementById("voting_system_page");
                        if(element != null) {
                            JSObject win = (JSObject) webView.getEngine().executeScript("window");
                            win.setMember("clientTool", new BrowserVSClient(webView));
                            webView.getEngine().executeScript(Utils.getSessionCoreSignalJSCommand(
                                    BrowserVSSessionUtils.getInstance().getBrowserSessionData()));
                        }
                    } else if (newState.equals(Worker.State.FAILED)) {
                        showMessage(new ResponseVS(ResponseVS.SC_ERROR, ContextVS.getMessage("connectionErrorMsg")));
                    } else if (newState.equals(Worker.State.SCHEDULED)) { }
                    if(newState.equals(Worker.State.FAILED) || newState.equals(Worker.State.SUCCEEDED)) {  }
                }
            }
        );
        VBox.setVgrow(webView, Priority.ALWAYS);
        Tab newTab = new Tab();
        newTab.setOnSelectionChanged(new EventHandler < Event > (){
            @Override public void handle(Event event) {
                int selectedIdx = tabPane.getSelectionModel().getSelectedIndex();
                ObservableList<WebHistory.Entry> entries = ((WebView)tabPane.getSelectionModel().getSelectedItem().
                        getContent()).getEngine().getHistory().getEntries();
                if(entries.size() > 0){
                    WebHistory.Entry selectedEntry = entries.get(entries.size() -1);
                    if(entries.size() > 1 &&  selectedEntry.getTitle() != null) newTab.setText(selectedEntry.getTitle());
                    log.debug("selectedIdx: " + selectedIdx + " - selectedEntry: " + selectedEntry);
                    locationField.setText(selectedEntry.getUrl());
                }
            }
        });
        if(tabCaption != null) newTab.setText(tabCaption.length() > MAX_CHARACTERS_TAB_CAPTION ?
                tabCaption.substring(0, MAX_CHARACTERS_TAB_CAPTION) + "...":tabCaption);
        else if(URL != null) newTab.setText(ContextVS.getMessage("loadingLbl") + " ...");
        else newTab.setText("                ");
        newTab.setContent(webView);
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
        if(URL != null) {
            PlatformImpl.runLater(() -> {
                webView.getEngine().load(URL);
                browserStage.show();
            });
        }
        browserStage.toFront();
        return webView;
    }

    private void consumeMessageTodDevice(final String pinDialogMessage) {
        PlatformImpl.runLater(() -> {
            if(BrowserVSSessionUtils.getCryptoTokenType() != CryptoTokenVS.MOBILE) {
                if(webSocketMessagePasswordDialog == null) {
                    webSocketMessagePasswordDialog = new PasswordDialog();
                    String dialogMessage = null;
                    if(pinDialogMessage == null) dialogMessage = ContextVS.getMessage("messageToDevicePasswordMsg");
                    else dialogMessage = pinDialogMessage;
                    webSocketMessagePasswordDialog.showWithoutPasswordConfirm(dialogMessage);
                    String password = webSocketMessagePasswordDialog.getPassword();
                    if(password != null) {
                        try {
                            KeyStore keyStore = ContextVS.getUserKeyStore(password.toCharArray());
                            PrivateKey privateKey = (PrivateKey)keyStore.getKey(ContextVS.KEYSTORE_USER_CERT_ALIAS,
                                    password.toCharArray());
                            InboxDialog.show(privateKey);
                        } catch(Exception ex) {
                            log.error(ex.getMessage(), ex);
                            showMessage(ResponseVS.SC_ERROR, ContextVS.getMessage("cryptoTokenPasswdErrorMsg"));
                        }
                    }
                    webSocketMessagePasswordDialog = null;
                } else webSocketMessagePasswordDialog.toFront();
            } else showMessage(new ResponseVS(ResponseVS.SC_ERROR, ContextVS.getMessage("messageToDeviceService") +
                    " - " + ContextVS.getMessage("jksRequiredMsg")));
        });
    }

    @Override public void sendMessageToBrowser(JSON messageJSON, String callerCallback) {
        String message = messageJSON.toString();
        String logMsg = message.length() > 300 ? message.substring(0, 300) + "..." : message;
        log.debug("sendMessageToBrowser - messageJSON: " + logMsg);
        try {
            WebView operationWebView = webViewMap.remove(callerCallback);
            final String jsCommand = "setClientToolMessage('" + callerCallback + "','" +
                    Base64.getEncoder().encodeToString(message.getBytes("UTF8")) + "')";
            PlatformImpl.runLater(() -> {  operationWebView.getEngine().executeScript(jsCommand); });
        } catch(Exception ex) { log.error(ex.getMessage(), ex); }
    }

    @Override public void processOperationVS(OperationVS operationVS, String passwordDialogMessage) {
        browserHelper.processOperationVS(operationVS, passwordDialogMessage);
    }

    @Override public void processOperationVS(String password, OperationVS operationVS) {
        browserHelper.processOperationVS(password, operationVS);
    }

    @Override public void processSignalVS(Map signalData) {//{title:, url:}
        log.debug("processSignalVS - caption: " + signalData.get("caption"));
        if(signalData.containsKey("caption")) tabPane.getSelectionModel().getSelectedItem().setText(
                (String)signalData.get("caption"));
    }

    public void showMessage(ResponseVS responseVS) {
        String message = responseVS.getMessage() == null? "":responseVS.getMessage();
        if(ResponseVS.SC_OK == responseVS.getStatusCode()) message = responseVS.getMessage();
        else message = ContextVS.getMessage("errorLbl") + " - " + responseVS.getMessage();
        showMessage(null, message);
    }

    public void showMessage(Integer statusCode, String message) {
        PlatformImpl.runLater(() -> {
            MessageDialog messageDialog = new MessageDialog();
            messageDialog.showMessage(statusCode, message);
        });
    }

    public void newTab(final String urlToLoad, String callback, String callbackMsg, final String caption,
            final boolean isToolbarVisible) {
        final StringBuilder jsCommand = new StringBuilder();
        if(callback != null && callbackMsg != null) jsCommand.append(callback + "(" + callbackMsg + ")");
        else if(callback != null) jsCommand.append(callback + "()");;
        log.debug("newTab - urlToLoad: " + urlToLoad + " - jsCommand: " + jsCommand.toString());
        newTab(urlToLoad, caption, "".equals(jsCommand.toString()) ? null : jsCommand.toString());
    }

    @Override public void consumeWebSocketMessage(WebSocketMessage message) {
        log.debug("consumeWebSocketMessage - operation: " + message.getOperation().toString());
        if(message.getStatusCode() != null && ResponseVS.SC_ERROR == message.getStatusCode()) {
            showMessage(message.getStatusCode(), message.getMessage());
            return;
        }
        switch(message.getOperation()) {
            case INIT_VALIDATED_SESSION:
                execCommandJS(message.getWebSocketCoreSignalJSCommand(WebSocketMessage.ConnectionStatus.OPEN));
                break;
            case MESSAGEVS_SIGN:
                log.debug("========= TODO MESSAGEVS_SIGN");
                break;
            case MESSAGEVS_TO_DEVICE:
                InboxManager.getInstance().addMessage(message);
                consumeMessageTodDevice(null);
                break;
            case MESSAGEVS_FROM_DEVICE:
                BrowserVSSessionUtils.setWebSocketMessage(message);
                break;
            default:
                log.debug("unprocessed message");
        }
    }

    @Override public void setConnectionStatus(WebSocketMessage.ConnectionStatus status) {
        log.debug("setConnectionStatus - status: " + status.toString());
        switch (status) {
            case CLOSED:
                execCommandJS(WebSocketMessage.getWebSocketCoreSignalJSCommand(
                        null, WebSocketMessage.ConnectionStatus.CLOSED));
                break;
            case OPEN:
                execCommandJS(WebSocketMessage.getWebSocketCoreSignalJSCommand(
                        null, WebSocketMessage.ConnectionStatus.OPEN));
                break;
        }
    }

    public void execCommandJS(String jsCommand) {
        PlatformImpl.runLater(() -> {
            for(WebView webView : webViewMap.values()) {
                webView.getEngine().executeScript(jsCommand);
            }
        });
    }

    public void execCommandJSCurrentView(String jsCommand) {
        PlatformImpl.runLater(() -> {
            ((WebView)tabPane.getSelectionModel().getSelectedItem().getContent()).getEngine().executeScript(jsCommand);
        });
    }

    public void registerCallerCallbackView(String callerCallback, WebView webView) {
        webViewMap.put(callerCallback, webView);
    }

    public WebSocketService getWebSocketService(){
        if(webSocketService == null) {
            webSocketService = new WebSocketService(ContextVS.getInstance().getVotingSystemSSLCerts(),
                    ContextVS.getInstance().getCooinServer());
            webSocketService.addListener(this);
        }
        return webSocketService;
    }

    public void sendWebSocketMessage(String message) throws IOException {
        PlatformImpl.runLater(() -> {
            try { getWebSocketService().sendMessage(message);}
            catch(Exception ex) {log.error(ex.getMessage(), ex);}
        });
    }

    public void sendWebSocketAuthenticatedMessage(String message) throws IOException {
        PlatformImpl.runLater(() -> {
            try { getWebSocketServiceAuthenticated().sendMessage(message);}
            catch(Exception ex) {log.error(ex.getMessage(), ex);}
        });
    }

    public WebSocketServiceAuthenticated getWebSocketServiceAuthenticated(){
        if(webSocketServiceAuthenticated == null) {
            webSocketServiceAuthenticated = new WebSocketServiceAuthenticated(ContextVS.getInstance().getVotingSystemSSLCerts(),
                    ContextVS.getInstance().getCooinServer());
            webSocketServiceAuthenticated.addListener(this);
        }
        return webSocketServiceAuthenticated;
    }

}