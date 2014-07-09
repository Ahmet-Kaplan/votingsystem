package org.votingsystem.client.util;

import com.sun.javafx.application.PlatformImpl;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import netscape.javascript.JSObject;
import org.apache.log4j.Logger;
import org.votingsystem.client.dialog.MessageDialog;
import org.votingsystem.client.pane.BrowserVSPane;
import org.votingsystem.model.ContentTypeVS;
import org.votingsystem.model.ContextVS;
import org.votingsystem.model.OperationVS;
import org.votingsystem.model.ResponseVS;
import org.votingsystem.util.FileUtils;
import org.votingsystem.util.HttpHelper;
import org.votingsystem.util.StringUtils;
import org.w3c.dom.Document;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class BrowserVS extends Region {

    private static Logger logger = Logger.getLogger(BrowserVS.class);

    private Stage browserStage;
    private HBox toolBar;
    private MessageDialog messageDialog;
    private WebView webView;
    private WebView smallView;
    private ComboBox comboBox;
    private VBox mainVBox;
    private BrowserVSPane browserHelper;
    private AtomicInteger offset = new AtomicInteger(0);


    public BrowserVS() {
        this(new WebView());
    }

    private BrowserVS(WebView webView) {
        this.webView = webView;
        this.webView.getEngine().setUserDataDirectory(new File(ContextVS.WEBVIEWDIR));
        Platform.setImplicitExit(false);
        SignatureService.getInstance().setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override public void handle(WorkerStateEvent t) {
                logger.debug("signatureService - OnSucceeded");
                PlatformImpl.runLater(new Runnable() {
                    @Override public void run() {
                        ResponseVS responseVS = SignatureService.getInstance().getValue();
                        if(ContentTypeVS.JSON == responseVS.getContentType()) {
                            sendMessageToBrowserApp(responseVS.getMessageJSON(),
                                    SignatureService.getInstance().getOperationVS().getCallerCallback());
                        } else sendMessageToBrowserApp(responseVS.getStatusCode(), responseVS.getMessage(),
                                SignatureService.getInstance().getOperationVS().getCallerCallback());
                    }
                });
            }
        });

        SignatureService.getInstance().setOnRunning(new EventHandler<WorkerStateEvent>() {
            @Override public void handle(WorkerStateEvent t) {
                logger.debug("signatureService - OnRunning");
            }
        });

        SignatureService.getInstance().setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override public void handle(WorkerStateEvent t) {
                logger.debug("signatureService - OnFailed");
            }
        });
        PlatformImpl.startup(new Runnable() {
            @Override
            public void run() {
                initComponents();
            }
        });
    }

    private void initComponents() {
        final WebHistory history = webView.getEngine().getHistory();
        smallView = new WebView();
        comboBox = new ComboBox();
        browserStage = new Stage();
        browserStage.initModality(Modality.WINDOW_MODAL);
        browserStage.setTitle(ContextVS.getMessage("mainDialogCaption"));
        browserStage.setResizable(true);
        browserStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                event.consume();
                browserStage.hide();
                logger.debug("browserStage.setOnCloseRequest");
            }
        });

        mainVBox = new VBox();
        VBox.setVgrow(webView, Priority.ALWAYS);

        final Button forwardButton = new Button();
        final Button prevButton = new Button();
        final Button reloadButton = new Button();

        forwardButton.setGraphic(new ImageView(Utils.getImage(this, "fa-chevron-right")));
        forwardButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override public void handle(javafx.event.ActionEvent ev) {
                try {
                    history.go(1);
                    prevButton.setDisable(false);
                } catch(Exception ex) {
                    forwardButton.setDisable(true);
                }
            }
        });

        prevButton.setGraphic(new ImageView(Utils.getImage(this, "fa-chevron-left")));
        prevButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override public void handle(javafx.event.ActionEvent ev) {
                try {
                    history.go(-1);
                    forwardButton.setDisable(false);
                } catch(Exception ex) {
                    prevButton.setDisable(true);
                }
            }
        });

        reloadButton.setGraphic(new ImageView(Utils.getImage(this, "fa-refresh")));
        reloadButton.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override public void handle(javafx.event.ActionEvent ev) {
                webView.getEngine().reload();
            }
        });

        prevButton.setDisable(true);
        forwardButton.setDisable(true);

        final TextField urlInputText = new TextField("");
        urlInputText.setPrefWidth(400);
        urlInputText.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override public void handle(KeyEvent ke) {
                if (ke.getCode().equals(KeyCode.ENTER)) {
                    if(!"".equals(urlInputText.getText())) {
                        String targetURL = null;
                        if(urlInputText.getText().startsWith("http://") || urlInputText.getText().startsWith("https://")) {
                            targetURL = urlInputText.getText().trim();
                        } else targetURL = "http://" + urlInputText.getText().trim();
                        loadURL(targetURL, null);
                    }
                }
            }
        });


        comboBox.setPrefWidth(300);
        toolBar = new HBox();
        toolBar.setAlignment(Pos.CENTER);
        toolBar.getStyleClass().add("browser-toolbar");
        toolBar.getChildren().addAll(prevButton, forwardButton, urlInputText, reloadButton, comboBox , createSpacer());

        //handle popup windows
        webView.getEngine().setCreatePopupHandler(
                new Callback<PopupFeatures, WebEngine>() {
                    @Override
                    public WebEngine call(PopupFeatures config) {
                        //smallView.setFontScale(0.8);
                        new BrowserVS(smallView).show(700, 700, false);
                        return smallView.getEngine();
                    }
                }
        );

        history.getEntries().addListener(new ListChangeListener<WebHistory.Entry>(){
            @Override
        public void onChanged(Change<? extends WebHistory.Entry> c) {
                c.next();
                for (WebHistory.Entry e : c.getRemoved()) {
                    comboBox.getItems().remove(e.getUrl());
                }
                for (WebHistory.Entry e : c.getAddedSubList()) {
                    comboBox.getItems().add(e.getUrl());
                }
                if(history.getCurrentIndex() > 0) prevButton.setDisable(false);
                logger.debug("== currentIndex= " + history.getCurrentIndex() + " - num. entries: " + history.getEntries().size());
            }
        });

        //set the behavior for the history combobox
        comboBox.setOnAction(new EventHandler<javafx.event.ActionEvent>() {
            @Override
            public void handle(javafx.event.ActionEvent ev) {
                int offset = comboBox.getSelectionModel().getSelectedIndex() - history.getCurrentIndex();
                history.go(offset);
            }
        });

        // process page loading
        webView.getEngine().getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
                    @Override
                    public void changed(ObservableValue<? extends Worker.State> ov,
                                        Worker.State oldState, Worker.State newState) {
                        //logger.debug("newState: " + newState);
                        if (newState == Worker.State.SUCCEEDED) {
                            JSObject win = (JSObject) webView.getEngine().executeScript("window");
                            win.setMember("clientTool", new JavafxClient());
                            webView.getEngine().executeScript("notifiyClientToolConnection()");
                        }else if (newState.equals(Worker.State.FAILED)) {
                            showMessage(ContextVS.getMessage("connectionErrorMsg"));
                        }
                        if(newState.equals(Worker.State.FAILED) || newState.equals(Worker.State.SUCCEEDED)) {
                        }
                    }
                }
        );
        mainVBox.getChildren().addAll(toolBar, webView);
        browserHelper = new BrowserVSPane();
        browserHelper.getChildren().add(0, mainVBox);

        Scene scene = new Scene(browserHelper, Color.web("#666970"));
        browserStage.setScene(scene);
        browserStage.setWidth(1050);
        browserStage.setHeight(1000);

        getChildren().addListener(new ListChangeListener<Node>() {
            @Override public void onChanged(Change<? extends Node> c) {}
        });

        webView.getEngine().documentProperty().addListener(new ChangeListener<Document>() {
            @Override public void changed(ObservableValue<? extends Document> prop, Document oldDoc, Document newDoc) {
                if(ContextVS.getInstance().getBoolProperty(ContextVS.IS_DEBUG_ENABLED, false)) enableFirebug( webView.getEngine());
            }
        });
    }

    public void sendMessageToBrowserApp(int statusCode, String message, String callbackFunction) {
        logger.debug("sendMessageToBrowserApp - statusCode: " + statusCode + " - message: " + message);
        Map resultMap = new HashMap();
        resultMap.put("statusCode", statusCode);
        resultMap.put("message", message);
        JSONObject messageJSON = (JSONObject)JSONSerializer.toJSON(resultMap);
        final String jsCommand = callbackFunction + "('" + StringUtils.escapeStringJS(messageJSON.toString()) + "')";
        logger.debug("sendMessageToBrowserApp - jsCommand: " + jsCommand);
        PlatformImpl.runLater(new Runnable() {
            @Override public void run() {
                webView.getEngine().executeScript(jsCommand);
            }
        });
    }

    public void sendMessageToBrowserApp(JSONObject messageJSON, String callbackFunction) {
        logger.debug("sendMessageToBrowserApp - messageJSON: " + messageJSON.toString());
        final String jsCommand = callbackFunction + "(" + messageJSON.toString() + ")";
        logger.debug("sendMessageToBrowserApp - jsCommand: " + jsCommand);
        PlatformImpl.runLater(new Runnable() {
            @Override public void run() {
                webView.getEngine().executeScript(jsCommand);
            }
        });
    }

    /**
     * waiting until the WebView has loaded a document before trying to trigger Firebug.
     * http://stackoverflow.com/questions/17387981/javafx-webview-webengine-firebuglite-or-some-other-debugger
     * Enables Firebug Lite for debugging a webEngine.
     * @param engine the webEngine for which debugging is to be enabled.
     */
    private static void enableFirebug(final WebEngine engine) {
        engine.executeScript("if (!document.getElementById('FirebugLite')){E = document['createElement' + 'NS'] && " +
                "document.documentElement.namespaceURI;E = E ? document['createElement' + 'NS'](E, 'script') : " +
                "document['createElement']('script');E['setAttribute']('id', 'FirebugLite');E['setAttribute']('src', " +
                "'https://getfirebug.com/' + 'firebug-lite.js' + '#startOpened');E['setAttribute']('FirebugLite', '4');" +
                "(document['getElementsByTagName']('head')[0] || document['getElementsByTagName']('body')[0]).appendChild(E);" +
                "E = new Image;E['setAttribute']('src', 'https://getfirebug.com/' + '#startOpened');}");
    }


    public void showMessage(ResponseVS responseVS) {
        String finalMsg = responseVS.getMessage() == null? "":responseVS.getMessage();
        if(ResponseVS.SC_OK == responseVS.getStatusCode()) finalMsg = responseVS.getMessage();
        else finalMsg = ContextVS.getMessage("errorLbl") + " - " + responseVS.getMessage();
        showMessage(finalMsg);
    }

    public void showMessage(final String message) {
        PlatformImpl.runLater(new Runnable() {
            @Override public void run() {
                if(messageDialog == null) messageDialog = new MessageDialog();
                messageDialog.showMessage(message);
            }
        });
    }


    public void loadURL(final String urlToLoad, final String caption) {

        PlatformImpl.runLater(new Runnable() {
            @Override public void run() {
                webView.getEngine().load(urlToLoad);
                if(caption != null) browserStage.setTitle(caption);
                browserStage.show();
            }
        });
    }

    public void loadURL(final String urlToLoad, String callback, String callbackMsg, final String caption,
            final boolean isToolbarVisible) {
        logger.debug("loadURL: " + urlToLoad);
        final StringBuilder jsCommand = new StringBuilder();
        if(callback != null && callbackMsg != null) jsCommand.append(callback + "(" + callbackMsg + ")");
        else if(callback != null) jsCommand.append(callback + "()");;
        logger.debug("jsCommand: " + jsCommand.toString());
        if(!"".equals(jsCommand.toString())) {
            webView.getEngine().getLoadWorker().stateProperty().addListener(
                    new ChangeListener<Worker.State>() {
                        @Override
                        public void changed(ObservableValue<? extends Worker.State> ov,
                                            Worker.State oldState, Worker.State newState) {
                            //logger.debug("newState: " + newState);
                            if (newState == Worker.State.SUCCEEDED) {
                                webView.getEngine().executeScript(jsCommand.toString());
                            }
                        }
                    }
            );
        }
        PlatformImpl.runLater(new Runnable() {
            @Override public void run() {
                if(!isToolbarVisible) mainVBox.getChildren().removeAll(toolBar);
                webView.getEngine().load(urlToLoad);
                if(caption != null) browserStage.setTitle(caption);
                browserStage.show();
            }
        });
    }

    public void executeScript (String jsCommand) {
        webView.getEngine().executeScript(jsCommand);
    }

    public void loadBackgroundURL(final String urlToLoad) {
        logger.debug("loadBackgroundURL: " + urlToLoad);
        PlatformImpl.runLater(new Runnable() {
            @Override public void run() {
                webView.getEngine().load(urlToLoad);
            }
        });
    }

    private void show(final int width, final int height, final boolean isToolbarVisible) {
        PlatformImpl.runLater(new Runnable() {
            @Override public void run() {
                browserStage.setWidth(width);
                browserStage.setHeight(height);
                if(!isToolbarVisible) mainVBox.getChildren().removeAll(toolBar);
                browserStage.show();
            }
        });
    }

    // JavaScript interface object
    public class JavafxClient {

        public void setJSONMessageToSignatureClient(String messageToSignatureClient) {
            logger.debug("JavafxClient.setJSONMessageToSignatureClient: " + messageToSignatureClient);
            try {
                JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(messageToSignatureClient);
                OperationVS operationVS = OperationVS.populate(jsonObject);
                switch(operationVS.getType()) {
                    case ANONYMOUS_REPRESENTATIVE_SELECTION_CANCELLED:
                        receiptCancellation(operationVS);
                        break;
                    case SELECT_IMAGE:
                        selectImage(operationVS);
                        break;
                    case OPEN_RECEIPT:
                        openReceipt(operationVS.getMessage(), operationVS.getCallerCallback());
                        break;
                    case SAVE_RECEIPT:
                        saveReceipt(operationVS.getMessage(), operationVS.getCallerCallback());
                        break;
                    case SAVE_RECEIPT_ANONYMOUS_DELEGATION:
                        break;
                    case MESSAGEVS_GET:
                        JSONObject documentJSON = (JSONObject)JSONSerializer.toJSON(operationVS.getDocument());
                        WebSocketService.getInstance().sendMessage(documentJSON.toString());
                        break;
                    /*case VICKET_SOURCE_NEW:
                        PEMCertFormPane.showDialog();
                        String pemCert = PEMCertFormPane.getCertChainPEM();
                        if(pemCert != null) {
                            operationVS.getDocumentToSignMap().put("certChainPEM", pemCert);
                            browserHelper.processOperationVS(operationVS);
                        }
                        break;*/
                    default:
                        browserHelper.processOperationVS(operationVS);
                }
            } catch(Exception ex) {
                showMessage( ContextVS.getMessage("errorLbl") + " - " + ex.getMessage());
            }
        }

    }

    private void saveReceiptAnonymousDelegation(String messageToSignatureClient, String callbackFunction) throws Exception{
        logger.debug("saveReceiptAnonymousDelegation");
        ResponseVS responseVS = ContextVS.getInstance().getHashCertVSData(messageToSignatureClient);
        if(responseVS == null) {
            logger.error("Missing receipt data for hash: " + messageToSignatureClient);
            sendMessageToBrowserApp(ResponseVS.SC_ERROR, null, callbackFunction);
        } else {
            File fileToSave = Utils.getAnonymousRepresentativeSelectCancellationFile(responseVS);
            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Zip (*.zip)",
                    "*" + ContentTypeVS.ZIP.getExtension());
            fileChooser.getExtensionFilters().add(extFilter);
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            fileChooser.setInitialFileName(ContextVS.getMessage("anonymousDelegationReceiptFileName"));
            File file = fileChooser.showSaveDialog(browserStage);
            if(file != null){
                FileUtils.copyStreamToFile(new FileInputStream(fileToSave), file);
                sendMessageToBrowserApp(ResponseVS.SC_OK, null, callbackFunction);
            } else sendMessageToBrowserApp(ResponseVS.SC_ERROR, null, callbackFunction);
        }
    }

    private void saveReceipt(String messageToSignatureClient, String callbackFunction) throws Exception{
        logger.debug("saveReceipt");
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                ContextVS.getMessage("signedFileFileFilterMsg"), "*" + ContentTypeVS.SIGNED.getExtension());
        fileChooser.getExtensionFilters().add(extFilter);
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialFileName(ContextVS.getMessage("genericReceiptFileName"));
        File file = fileChooser.showSaveDialog(browserStage);
        if(file != null){
            Utils.saveFile(messageToSignatureClient, file);
            sendMessageToBrowserApp(ResponseVS.SC_OK, null, callbackFunction);
        } else sendMessageToBrowserApp(ResponseVS.SC_ERROR, null, callbackFunction);
    }

    private void openReceipt(String messageToSignatureClient, String callbackFunction) throws Exception{
        logger.debug("openReceipt");
        SignedDocumentsBrowser.showDialog(messageToSignatureClient);
    }

    private void selectImage(final OperationVS operationVS) throws Exception {
        PlatformImpl.runLater(new Runnable() {
            @Override public void run() {
                try {
                    FileChooser fileChooser = new FileChooser();
                    FileChooser.ExtensionFilter extFilterJPG = new FileChooser.ExtensionFilter(
                            "JPG (*.jpg)", Arrays.asList("*.jpg", "*.JPG"));
                    FileChooser.ExtensionFilter extFilterPNG = new FileChooser.ExtensionFilter(
                            "PNG (*.png)", Arrays.asList("*.png", "*.PNG"));
                    fileChooser.getExtensionFilters().addAll(extFilterJPG, extFilterPNG);
                    fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                    File selectedImage = fileChooser.showOpenDialog(null);
                    if(selectedImage != null){
                        byte[] imageFileBytes = FileUtils.getBytesFromFile(selectedImage);
                        logger.debug(" - imageFileBytes.length: " + imageFileBytes.length);
                        if(imageFileBytes.length > ContextVS.IMAGE_MAX_FILE_SIZE) {
                            logger.debug(" - MAX_FILE_SIZE exceeded ");
                            sendMessageToBrowserApp(ResponseVS.SC_ERROR,
                                    ContextVS.getMessage("fileSizeExceeded", ContextVS.IMAGE_MAX_FILE_SIZE_KB),
                                    operationVS.getCallerCallback());
                        } else sendMessageToBrowserApp(ResponseVS.SC_OK, selectedImage.getAbsolutePath(),
                                operationVS.getCallerCallback());
                    } else sendMessageToBrowserApp(ResponseVS.SC_ERROR, null, operationVS.getCallerCallback());
                } catch(Exception ex) {
                    sendMessageToBrowserApp(ResponseVS.SC_ERROR, ex.getMessage(), operationVS.getCallerCallback());
                }
            }
        });
    }

    private void receiptCancellation(final OperationVS operationVS) throws Exception {
        logger.debug("receiptCancellation");
        switch(operationVS.getType()) {
            case ANONYMOUS_REPRESENTATIVE_SELECTION_CANCELLED:
                PlatformImpl.runLater(new Runnable() {
                    @Override public void run() {
                        FileChooser fileChooser = new FileChooser();
                        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Zip (*.zip)",
                                "*" + ContentTypeVS.ZIP.getExtension());
                        fileChooser.getExtensionFilters().add(extFilter);
                        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
                        File file = fileChooser.showSaveDialog(browserStage);
                        if(file != null){
                            operationVS.setFile(file);
                            browserHelper.processOperationVS(operationVS);
                        } else sendMessageToBrowserApp(ResponseVS.SC_ERROR, null, operationVS.getCallerCallback());
                    }
                });
                break;
            default:
                logger.debug("receiptCancellation - unknown receipt type: " + operationVS.getType());
        }
    }

    private Node createSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    @Override protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double tbHeight = toolBar.prefHeight(w);
        layoutInArea(webView,0,0,w,h-tbHeight,0, HPos.CENTER, VPos.CENTER);
        layoutInArea(toolBar,0,h-tbHeight,w,tbHeight,0,HPos.CENTER,VPos.CENTER);
    }

    @Override protected double computeMinWidth(double height) {
        return 1000;
    }

    @Override protected double computeMinHeight(double width) {
        return 1000;
    }

}