package org.votingsystem.client.dialog;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.votingsystem.client.Browser;
import org.votingsystem.client.service.BrowserSessionService;
import org.votingsystem.client.util.Utils;
import org.votingsystem.dto.CertExtensionDto;
import org.votingsystem.dto.DeviceVSDto;
import org.votingsystem.model.ResponseVS;
import org.votingsystem.model.UserVS;
import org.votingsystem.signature.util.CertUtils;
import org.votingsystem.signature.util.CryptoTokenVS;
import org.votingsystem.signature.util.KeyStoreUtil;
import org.votingsystem.util.ContextVS;
import org.votingsystem.util.FileUtils;
import org.votingsystem.util.TypeVS;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.votingsystem.client.Browser.showMessage;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class SettingsDialog extends DialogVS  implements MobileSelectorDialog.Listener, PasswordDialog.Listener {

    private static Logger log = Logger.getLogger(SettingsDialog.class.getSimpleName());


    private KeyStore selectedKeyStore;
    private Label keyStoreLbl;
    private HBox mobileDeviceInfo;
    private Label mobileDeviceLbl;
    private VBox keyStoreVBox;
    private GridPane gridPane;
    private RadioButton signWithDNIeRb;
    private RadioButton signWithMobileRb;
    private RadioButton signWithKeystoreRb;
    private DeviceVSDto deviceVSDto;

    public SettingsDialog() {
        super(new GridPane());
        final Button acceptButton = new Button(ContextVS.getMessage("acceptLbl"));
        gridPane = (GridPane) getContentPane();
        gridPane.setVgap(15);
        ToggleGroup tg = new ToggleGroup();
        signWithDNIeRb = new RadioButton(ContextVS.getMessage("setDNIeSignatureMechanismMsg"));
        signWithDNIeRb.setToggleGroup(tg);
        signWithDNIeRb.setOnAction(event -> changeCryptoToken(event));
        signWithMobileRb = new RadioButton(ContextVS.getMessage("setMobileSignatureMechanismMsg"));
        signWithMobileRb.setToggleGroup(tg);
        signWithMobileRb.setOnAction(event -> changeCryptoToken(event));
        mobileDeviceInfo = new HBox(15);
        mobileDeviceInfo.setAlignment(Pos.CENTER);
        mobileDeviceLbl = new Label();
        mobileDeviceLbl.setStyle("-fx-text-fill: #888;");
        Button mobileDeviceButton = new Button(ContextVS.getMessage("changeMobileDeviceLbl"));
        mobileDeviceButton.setGraphic(Utils.getIcon(FontAwesomeIcons.EXCHANGE));
        mobileDeviceButton.setOnAction(actionEvent -> MobileSelectorDialog.show(ContextVS.getMessage(
                "setMobileSignatureMechanismMsg"), ContextVS.getMessage("setMobileSignatureMechanismAdv"),
                SettingsDialog.this));
        mobileDeviceInfo.getChildren().addAll(mobileDeviceLbl, mobileDeviceButton);
        signWithKeystoreRb = new RadioButton(ContextVS.getMessage("setJksKeyStoreSignatureMechanismMsg"));
        signWithKeystoreRb.setToggleGroup(tg);
        signWithKeystoreRb.setOnAction(actionEvent -> changeCryptoToken(actionEvent));
        keyStoreVBox = new VBox(10);
        Button selectKeyStoreButton = new Button(ContextVS.getMessage("setKeyStoreLbl"));
        selectKeyStoreButton.setGraphic(Utils.getIcon(FontAwesomeIcons.KEY));
        selectKeyStoreButton.setOnAction(actionEvent -> selectKeystoreFile());
        keyStoreLbl = new Label(ContextVS.getMessage("selectKeyStoreLbl"));
        keyStoreLbl.setContentDisplay(ContentDisplay.LEFT);
        keyStoreVBox.getChildren().addAll(selectKeyStoreButton, keyStoreLbl);
        VBox.setMargin(keyStoreVBox, new Insets(10, 10, 10, 10));
        keyStoreVBox.getStyleClass().add("pane-border");
        Button requestCertButton = new Button(ContextVS.getMessage("requestCertLbl"));
        requestCertButton.setGraphic(Utils.getIcon(FontAwesomeIcons.CERTIFICATE));
        requestCertButton.setOnAction(actionEvent -> {
            if (ContextVS.getInstance().getAccessControl() != null) {
                Platform.runLater(() -> {
                    Browser.getInstance().newTab(
                            ContextVS.getInstance().getAccessControl().getCertRequestServiceURL(),null, null);
                    getStage().close();
                });
            } else {
                log.log(Level.SEVERE, "missing 'access control'");
                showMessage(ResponseVS.SC_ERROR, ContextVS.getMessage("connectionErrorMsg"));
            }
        });

        HBox footerButtonsBox = new HBox(10);
        Button cancelButton = new Button(ContextVS.getMessage("cancelLbl"));
        cancelButton.setGraphic(Utils.getIcon(FontAwesomeIcons.TIMES, Utils.COLOR_RED_DARK));
        cancelButton.setOnAction(actionEvent -> getStage().close());
        acceptButton.setGraphic(Utils.getIcon(FontAwesomeIcons.CHECK));
        acceptButton.setOnAction(actionEvent -> validateForm());
        footerButtonsBox.getChildren().addAll(Utils.getSpacer(), cancelButton, acceptButton);
        gridPane.setMargin(footerButtonsBox, new Insets(20, 20, 0, 20));
        gridPane.add(requestCertButton, 0, 0);
        gridPane.setMargin(requestCertButton, new Insets(10, 20, 20, 20));
        gridPane.add(signWithMobileRb, 0, 1);
        gridPane.setMargin(mobileDeviceInfo, new Insets(0, 0, 0, 20));
        gridPane.add(signWithDNIeRb, 0, 3);
        gridPane.add(signWithKeystoreRb,0,5);
        gridPane.add(footerButtonsBox,0,7);
        gridPane.getStylesheets().add(Utils.getResource("/css/modal-dialog.css"));
        gridPane.getStyleClass().add("modal-dialog");
        gridPane.setMinWidth(600);
    }

    private void changeCryptoToken(ActionEvent evt) {
        log.info("changeCryptoToken");
        if(gridPane.getChildren().contains(keyStoreVBox)) gridPane.getChildren().remove(keyStoreVBox);
        if(gridPane.getChildren().contains(mobileDeviceInfo)) gridPane.getChildren().remove(mobileDeviceInfo);
        if(evt.getSource() == signWithKeystoreRb) {
            gridPane.add(keyStoreVBox, 0, 6);
            if(BrowserSessionService.getInstance().getKeyStoreUserVS() != null)
                keyStoreLbl.setText(BrowserSessionService.getInstance().getKeyStoreUserVS().getName());
        }
        if(evt.getSource() == signWithMobileRb && deviceVSDto != null) {
            gridPane.add(mobileDeviceInfo, 0, 2);
        }
        if(evt.getSource() == signWithMobileRb) MobileSelectorDialog.show(ContextVS.getMessage(
                "setMobileSignatureMechanismMsg"), ContextVS.getMessage("setMobileSignatureMechanismAdv"),
                SettingsDialog.this);
        getStage().sizeToScene();
    }

    @Override public void show() {
        log.info("show");
        selectedKeyStore = null;
        gridPane.getChildren().remove(keyStoreVBox);
        switch(BrowserSessionService.getCryptoTokenType()) {
            case DNIe:
                signWithDNIeRb.setSelected(true);
                deviceVSDto = null;
                break;
            case JKS_KEYSTORE:
                signWithKeystoreRb.setSelected(true);
                deviceVSDto = BrowserSessionService.getInstance().getCryptoToken();
                gridPane.add(keyStoreVBox, 0, 6);
                if(BrowserSessionService.getInstance().getKeyStoreUserVS() != null)
                    keyStoreLbl.setText(BrowserSessionService.getInstance().getKeyStoreUserVS().getName());
                break;
            case MOBILE:
                signWithMobileRb.setSelected(true);
                deviceVSDto = BrowserSessionService.getInstance().getCryptoToken();
                mobileDeviceLbl.setText(deviceVSDto.getDeviceName());
                gridPane.add(mobileDeviceInfo, 0, 2);
                break;
        }
        getStage().show();
        BrowserSessionService.getInstance().checkCSRRequest();
    }

    public static void showDialog() {
        Platform.runLater(() -> {
            SettingsDialog settingsDialog = new SettingsDialog();
            settingsDialog.show();
        });
    }

    private void selectKeystoreFile() {
        log.info("selectKeystoreFile");
        try {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle(ContextVS.getMessage("selectKeyStore"));
            File file = fileChooser.showOpenDialog(getStage());
            if (file != null) {
                File selectedKeystore = new File(file.getAbsolutePath());
                byte[] keystoreBytes = FileUtils.getBytesFromFile(selectedKeystore);
                try {
                    selectedKeyStore = KeyStoreUtil.getKeyStoreFromBytes(keystoreBytes, null);
                } catch(Exception ex) {
                    showMessage(ResponseVS.SC_ERROR, ContextVS.getMessage("keyStoreNotValidErrorMsg"));
                }
                X509Certificate certSigner = (X509Certificate) selectedKeyStore.getCertificate("UserTestKeysStore");
                keyStoreLbl.setText(certSigner.getSubjectDN().toString());
            } else {
                keyStoreLbl.setText(ContextVS.getMessage("selectKeyStoreLbl"));
            }
        } catch (Exception ex) {
            log.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private void validateForm() {
        log.info("validateForm");
        if(signWithKeystoreRb.isSelected()) {
            if(selectedKeyStore != null) {
                PasswordDialog.showWithPasswordConfirm(TypeVS.KEYSTORE_SELECT, this,
                        ContextVS.getMessage("newKeyStorePasswordMsg"));
                return;
            }
            if(selectedKeyStore == null && BrowserSessionService.getInstance().getKeyStoreUserVS() == null) {
                showMessage(ResponseVS.SC_ERROR, ContextVS.getMessage("keyStoreNotSelectedErrorLbl"));
                return;
            }
        }
        close();
    }

    private void close() {
        if(signWithDNIeRb.isSelected()) {
            deviceVSDto = new DeviceVSDto(CryptoTokenVS.DNIe);
        } else if(signWithMobileRb.isSelected()) {
            if(deviceVSDto == null) {
                showMessage(ResponseVS.SC_ERROR, ContextVS.getMessage("deviceDataMissingErrorMsg"));
                return;
            } else deviceVSDto.setType(CryptoTokenVS.MOBILE);
        }
        BrowserSessionService.getInstance().setCryptoToken(deviceVSDto);
        hide();
    }

    @Override public void setSelectedDevice(DeviceVSDto selectedDevice) {
        log.info("setSelectedDevice: " + selectedDevice.getDeviceName());
        this.deviceVSDto = selectedDevice;
        if(!gridPane.getChildren().contains(mobileDeviceInfo)) gridPane.add(mobileDeviceInfo, 0, 2);
        mobileDeviceLbl.setText(deviceVSDto.getDeviceName());
        getStage().sizeToScene();
    }

    @Override public void setPassword(TypeVS passwordType, String password) {
        switch (passwordType) {
            case KEYSTORE_SELECT:
                try {
                    UserVS userVS = ContextVS.saveUserKeyStore(selectedKeyStore, password);
                    CertExtensionDto certExtensionDto = CertUtils.getCertExtensionData(CertExtensionDto.class,
                            userVS.getCertificate(), ContextVS.DEVICEVS_OID);
                    deviceVSDto = new DeviceVSDto(userVS, certExtensionDto);
                    deviceVSDto.setType(CryptoTokenVS.JKS_KEYSTORE);
                    deviceVSDto.setDeviceName(userVS.getNif() + " - " + userVS.getName());
                    close();
                } catch (Exception ex) {
                    log.log(Level.SEVERE, ex.getMessage(), ex);
                    showMessage(ResponseVS.SC_ERROR, ex.getMessage());
                }
                break;
        }
    }

}