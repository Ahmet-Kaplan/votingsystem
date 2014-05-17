package org.votingsystem.client.dialog;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.*;
import org.apache.log4j.Logger;
import org.votingsystem.client.util.Utils;
import org.votingsystem.model.ContextVS;
import org.votingsystem.signature.util.ContentSignerHelper;
import org.votingsystem.signature.util.KeyStoreUtil;
import org.votingsystem.util.FileUtils;
import org.votingsystem.util.NifUtils;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * @author jgzornoza
 * Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
 */
public class SettingsDialog {

    private static Logger logger = Logger.getLogger(SettingsDialog.class);

    private Stage stage;
    private KeyStore userKeyStore;
    private Label keyStoreLabel;
    private VBox keyStoreVBox;
    private VBox mobileVBox;
    private GridPane gridPane;
    //private VBox dialogVBox;
    private RadioButton signWithDNIeRb;
    private RadioButton signWithMobileRb;
    private TextField mobileNIFTextField;
    private RadioButton signWithKeystoreRb;

    public SettingsDialog() {
        stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);
        final Button acceptButton = new Button(ContextVS.getMessage("acceptLbl"));

        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
            @Override public void handle(WindowEvent window) {      }
        });

        gridPane = new GridPane();
        gridPane.setVgap(10);


        ToggleGroup tg = new ToggleGroup();

        signWithDNIeRb = new RadioButton(ContextVS.getMessage("setDNIeSignatureMechanismMsg"));
        signWithDNIeRb.setToggleGroup(tg);
        signWithDNIeRb.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
                changeSignatureMode(actionEvent);
            }});


        signWithMobileRb = new RadioButton(ContextVS.getMessage("setAndroidSignatureMechanismMsg"));
        signWithMobileRb.setToggleGroup(tg);
        signWithMobileRb.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
                changeSignatureMode(actionEvent);
            }});
        mobileVBox = new VBox(10);
        Label label = new Label(ContextVS.getMessage("mobileNIFLbl") + ":");
        mobileNIFTextField = new TextField();
        mobileNIFTextField.addEventHandler(KeyEvent.KEY_PRESSED,
                new EventHandler<KeyEvent>() {
                    public void handle(KeyEvent event) {
                        if ((event.getCode() == KeyCode.ENTER)) {
                            acceptButton.fire();
                        }
                    }
                }
        );
        mobileNIFTextField.setPromptText(ContextVS.getMessage("nifLbl"));
        mobileVBox.getChildren().addAll(label, mobileNIFTextField);
        mobileVBox.getStyleClass().add("settings-vbox");

        signWithKeystoreRb = new RadioButton(ContextVS.getMessage("setJksKeyStoreSignatureMechanismMsg"));
        signWithKeystoreRb.setToggleGroup(tg);
        signWithKeystoreRb.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
                changeSignatureMode(actionEvent);
            }});
        keyStoreVBox = new VBox(10);

        Button selectKeyStoreButton = new Button(ContextVS.getMessage("setKeyStoreLbl"));
        selectKeyStoreButton.setGraphic(new ImageView(Utils.getImage(this, "fa-key")));
        selectKeyStoreButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
                selectKeystoreFile();
            }});
        keyStoreLabel = new Label(ContextVS.getMessage("selectKeyStoreLbl"));
        keyStoreLabel.setContentDisplay(ContentDisplay.LEFT);
        keyStoreVBox.getChildren().addAll(selectKeyStoreButton, keyStoreLabel);
        VBox.setMargin(keyStoreVBox, new Insets(10, 10, 10, 10));
        keyStoreVBox.getStyleClass().add("settings-vbox");

        HBox footerButtonsBox = new HBox(10);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        acceptButton.setGraphic(new ImageView(Utils.getImage(this, "accept")));
        acceptButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
                validateForm();
            }});
        footerButtonsBox.getChildren().addAll(spacer, acceptButton);
        VBox.setMargin(footerButtonsBox, new Insets(20, 10, 0, 0));


        gridPane.add(signWithDNIeRb,0,0);
        gridPane.add(signWithMobileRb,0,2);
        gridPane.add(signWithKeystoreRb,0,4);
        gridPane.add(footerButtonsBox,0,6);
        gridPane.getStyleClass().add("modal-dialog");
        stage.setScene(new Scene(gridPane, Color.TRANSPARENT));
        stage.getScene().getStylesheets().add(getClass().getResource("/resources/css/modal-dialog.css").toExternalForm());
        // allow the dialog to be dragged around.
        final Node root = stage.getScene().getRoot();
        final Delta dragDelta = new Delta();
        root.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                // record a delta distance for the drag and drop operation.
                dragDelta.x = stage.getX() - mouseEvent.getScreenX();
                dragDelta.y = stage.getY() - mouseEvent.getScreenY();
            }
        });
        root.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent mouseEvent) {
                stage.setX(mouseEvent.getScreenX() + dragDelta.x);
                stage.setY(mouseEvent.getScreenY() + dragDelta.y);
            }
        });
        gridPane.setMinWidth(600);
    }

    private void changeSignatureMode(ActionEvent evt) {
        logger.debug("changeSignatureMode");
        gridPane.getChildren().remove(keyStoreVBox);
        gridPane.getChildren().remove(mobileVBox);
        if(evt.getSource() == signWithKeystoreRb) {
            gridPane.add(keyStoreVBox, 0, 5);
        } else if(evt.getSource() == signWithMobileRb){
            String userNif = ContextVS.getInstance().getProperty(ContextVS.USER_NIF, null);
            if(userNif != null && !"".equals(userNif.trim())) mobileNIFTextField.setText(userNif);
            gridPane.add(mobileVBox, 0, 3);
        }
        stage.sizeToScene();
    }

    public void show() {
        logger.debug("show");
        String cryptoTokenStr = ContextVS.getInstance().getProperty(ContextVS.CRYPTO_TOKEN,
                ContentSignerHelper.CryptoToken.DNIe.toString());
        ContentSignerHelper.CryptoToken cryptoToken = ContentSignerHelper.CryptoToken.valueOf(cryptoTokenStr);
        gridPane.getChildren().remove(keyStoreVBox);
        gridPane.getChildren().remove(mobileVBox);
        switch(cryptoToken) {
            case MOBILE:
                signWithMobileRb.setSelected(true);
                String userNif = ContextVS.getInstance().getProperty(ContextVS.USER_NIF, null);
                if(userNif != null && !"".equals(userNif.trim())) mobileNIFTextField.setText(userNif);
                gridPane.add(mobileVBox, 0, 3);
                break;
            case DNIe:
                signWithDNIeRb.setSelected(true);
                break;
            case JKS_KEYSTORE:
                signWithKeystoreRb.setSelected(true);
                gridPane.add(keyStoreVBox, 0, 5);
                break;
        }
        stage.centerOnScreen();
        stage.show();
    }

    private void selectKeystoreFile() {
        logger.debug("selectKeystoreFile");
        try {
            final FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                File selectedKeystore = new File(file.getAbsolutePath());
                byte[] keystoreBytes = FileUtils.getBytesFromFile(selectedKeystore);
                try {
                    userKeyStore = KeyStoreUtil.getKeyStoreFromBytes(keystoreBytes, null);
                } catch(Exception ex) {
                    FXMessageDialog messageDialog = new FXMessageDialog();
                    messageDialog.showMessage(ContextVS.getMessage("errorLbl") + " " +
                            ContextVS.getMessage("keyStoreNotValidErrorMsg"));
                }
                //PrivateKey privateKeySigner = (PrivateKey)userKeyStore.getKey("UserTestKeysStore", null);
                X509Certificate certSigner = (X509Certificate) userKeyStore.getCertificate("UserTestKeysStore");
                keyStoreLabel.setText(certSigner.getSubjectDN().toString());
            } else {
                keyStoreLabel.setText(ContextVS.getMessage("selectKeyStoreLbl"));
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void validateForm() {
        logger.debug("validateForm");
        String cryptoTokenStr = ContextVS.getInstance().getProperty(ContextVS.CRYPTO_TOKEN,
                ContentSignerHelper.CryptoToken.DNIe.toString());
        ContentSignerHelper.CryptoToken cryptoToken = ContentSignerHelper.CryptoToken.valueOf(cryptoTokenStr);
        if(signWithKeystoreRb.isSelected() &&  ContentSignerHelper.CryptoToken.JKS_KEYSTORE != cryptoToken) {
            if(userKeyStore == null) {
                FXMessageDialog messageDialog = new FXMessageDialog();
                messageDialog.showMessage(ContextVS.getMessage("errorLbl") + " " +
                        ContextVS.getMessage("keyStoreNotSelectedErrorLbl"));
                return;
            }
        }
        if(userKeyStore != null) {
            if(signWithKeystoreRb.isSelected()) {
                try {
                    PasswordDialog passwordDialog = new PasswordDialog();
                    passwordDialog.show(ContextVS.getMessage("newKeyStorePasswordMsg"));
                    String password = passwordDialog.getPassword();
                    ContextVS.saveUserKeyStore(userKeyStore, password);
                    ContextVS.getInstance().setProperty(ContextVS.CRYPTO_TOKEN,
                            ContentSignerHelper.CryptoToken.JKS_KEYSTORE.toString());
                } catch(Exception ex) {
                    FXMessageDialog messageDialog = new FXMessageDialog();
                    messageDialog.showMessage(ContextVS.getMessage("errorLbl") + " " +
                            ContextVS.getMessage("errorStoringKeyStoreMsg"));
                    return;
                }
            }
        }
        if(signWithDNIeRb.isSelected()) {
            ContextVS.getInstance().setProperty(ContextVS.CRYPTO_TOKEN,
                    ContentSignerHelper.CryptoToken.DNIe.toString());
        }
        if(signWithMobileRb.isSelected()) {
            String userNif = NifUtils.validate(mobileNIFTextField.getText());
            if(userNif != null) {
                ContextVS.getInstance().setProperty(ContextVS.CRYPTO_TOKEN,
                        ContentSignerHelper.CryptoToken.MOBILE.toString());
                ContextVS.getInstance().setProperty(ContextVS.USER_NIF,userNif);
            } else {
                FXMessageDialog messageDialog = new FXMessageDialog();
                messageDialog.showMessage(ContextVS.getMessage("errorLbl") + " " +
                        ContextVS.getMessage("nifWithErrorsLbl"));
                return;
            }
        }
        stage.close();
    }

    class Delta { double x, y; }

}