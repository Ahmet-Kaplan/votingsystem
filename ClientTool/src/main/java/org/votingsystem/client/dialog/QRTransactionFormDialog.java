package org.votingsystem.client.dialog;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcons;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import org.votingsystem.client.Browser;
import org.votingsystem.client.VotingSystemApp;
import org.votingsystem.client.control.CurrencyCodeChoiceBox;
import org.votingsystem.client.control.NumberTextField;
import org.votingsystem.client.service.BrowserSessionService;
import org.votingsystem.client.service.EventBusService;
import org.votingsystem.client.service.WebSocketAuthenticatedService;
import org.votingsystem.client.util.Utils;
import org.votingsystem.dto.QRMessageDto;
import org.votingsystem.dto.SocketMessageDto;
import org.votingsystem.dto.currency.TransactionVSDto;
import org.votingsystem.model.ResponseVS;
import org.votingsystem.model.TagVS;
import org.votingsystem.model.UserVS;
import org.votingsystem.model.currency.TransactionVS;
import org.votingsystem.signature.util.CryptoTokenVS;
import org.votingsystem.util.ContextVS;
import org.votingsystem.util.JSON;
import org.votingsystem.util.QRUtils;
import org.votingsystem.util.TypeVS;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.votingsystem.client.Browser.showMessage;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class QRTransactionFormDialog extends DialogVS implements AddTagVSDialog.Listener {

    private static Logger log = Logger.getLogger(QRTransactionFormDialog.class.getSimpleName());

    @FXML private Label tagLbl;
    @FXML private Label userNameLbl;
    @FXML private Label userIBANLbl;
    @FXML private CheckBox timeLimitedCheckBox;
    @FXML private NumberTextField amounTxt;
    @FXML private TextField subjectTxt;
    @FXML private Button addTagButton;
    @FXML private Button acceptButton;
    @FXML private HBox tagHBox;
    @FXML private HBox imageHBox;
    @FXML private VBox mainPane;
    @FXML private VBox formVBox;
    @FXML private ImageView imageView;
    @FXML private CurrencyCodeChoiceBox currencyChoiceBox;
    private Image qrCodeImage;

    private String selectedTag = null;

    private static QRTransactionFormDialog INSTANCE;

    class EventBusSocketMsgListener {
        @Subscribe
        public void responseVSChange(SocketMessageDto socketMsg) {
            switch(socketMsg.getOperation()) {
                case TRANSACTIONVS_RESPONSE:
                    Platform.runLater(() -> { toggleView(true);});
                    break;
                default:log.info("EventBusSocketMsgListener - unprocessed operation: " + socketMsg.getOperation());
            }
        }
    }

    public QRTransactionFormDialog() throws IOException {
        super("/fxml/QRTransactionFormPane.fxml");
        EventBusService.getInstance().register(new EventBusSocketMsgListener());
    }

    @FXML void initialize() {// This method is called by the FXMLLoader when initialization is complete
        userNameLbl.setText(BrowserSessionService.getInstance().getUserVS().getName());
        userIBANLbl.setText(BrowserSessionService.getInstance().getConnectedDevice().getIBAN());
        subjectTxt.setPromptText(ContextVS.getMessage("subjectLbl"));
        addTagButton.setText(ContextVS.getMessage("addTagVSLbl"));

        addTagButton.setOnAction(actionEvent -> {
            if (selectedTag == null) {
                AddTagVSDialog.show(ContextVS.getMessage("addTagVSLbl"), this);
            } else {
                selectedTag = null;
                formVBox.getChildren().remove(timeLimitedCheckBox);
                addTagButton.setText(ContextVS.getMessage("addTagVSLbl"));
                tagLbl.setText(ContextVS.getMessage("addTagMsg"));
            }
        });
        addTagButton.setGraphic(Utils.getIcon(FontAwesomeIcons.TAG));
        timeLimitedCheckBox.setText(ContextVS.getMessage("timeLimitedCheckBox"));
        tagLbl.setText(ContextVS.getMessage("addTagMsg"));
        acceptButton.setText(ContextVS.getMessage("acceptLbl"));
        acceptButton.setGraphic(Utils.getIcon(FontAwesomeIcons.CHECK));
        acceptButton.setOnAction(actionEvent -> {
            Platform.runLater(() -> {
                try {
                    if (qrCodeImage == null) {
                        if (subjectTxt.getText().trim().equals("")) {
                            showMessage(ResponseVS.SC_ERROR, ContextVS.getMessage("emptyFieldErrorMsg",
                                    ContextVS.getMessage("subjectLbl")));
                            return;
                        }
                        TransactionVSDto dto = TransactionVSDto.PAYMENT_REQUEST(
                                BrowserSessionService.getInstance().getUserVS().getName(), UserVS.Type.USER,
                                new BigDecimal(amounTxt.getText()), currencyChoiceBox.getSelected(),
                                BrowserSessionService.getInstance().getConnectedDevice().getIBAN(), subjectTxt.getText(),
                                (selectedTag == null ? TagVS.WILDTAG : selectedTag));
                        dto.setPaymentOptions(Arrays.asList(TransactionVS.Type.FROM_USERVS,
                                TransactionVS.Type.CURRENCY_SEND, TransactionVS.Type.CURRENCY_CHANGE));
                        if ((dto.getAmount().compareTo(BigDecimal.ONE) < 0)) {
                            showMessage(ResponseVS.SC_ERROR, ContextVS.getMessage("amountErrorMsg"));
                            return;
                        }
                        dto.setTimeLimited(timeLimitedCheckBox.isSelected());
                        QRMessageDto qrDto = new QRMessageDto(BrowserSessionService.getInstance().getConnectedDevice(),
                                TypeVS.TRANSACTIONVS_INFO);
                        qrDto.setData(dto);
                        qrCodeImage = new Image(new ByteArrayInputStream(QRUtils.encodeAsPNG(
                                JSON.getMapper().writeValueAsString(qrDto), 500, 500)));
                        imageView.setImage(qrCodeImage);
                        VotingSystemApp.getInstance().putQRMessage(qrDto);
                        toggleView(false);
                    } else {
                        toggleView(true);
                    }
                } catch (Exception ex) {
                    log.log(Level.SEVERE, ex.getMessage(), ex);
                }
            });
        });
        tagHBox.setStyle("-fx-border-color: #6c0404; -fx-border-width: 1;-fx-wrap-text: true;");
        mainPane.setStyle("-fx-font-size: 15; -fx-font-weight: bold;-fx-wrap-text: true; -fx-text-fill:#434343;");
        if(imageHBox.getChildren().contains(imageView)) imageHBox.getChildren().remove(imageView);
        tagLbl.setWrapText(true);
        formVBox.getChildren().remove(timeLimitedCheckBox);
    }

    private void toggleView(boolean isFormView) {
        log.info("toggleView");
        if(isFormView) {
            if (imageHBox.getChildren().contains(imageView)) imageHBox.getChildren().remove(imageView);
            if (!mainPane.getChildren().contains(formVBox)) mainPane.getChildren().add(0, formVBox);
            if (!mainPane.getChildren().contains(tagHBox)) mainPane.getChildren().add(1, tagHBox);
            acceptButton.setText(ContextVS.getMessage("acceptLbl"));
            qrCodeImage = null;
        } else {
            acceptButton.setText(ContextVS.getMessage("newLbl"));
            if (!imageHBox.getChildren().contains(imageView)) imageHBox.getChildren().add(imageView);
            if (mainPane.getChildren().contains(formVBox)) mainPane.getChildren().remove(formVBox);
            if (mainPane.getChildren().contains(tagHBox)) mainPane.getChildren().remove(tagHBox);
        }
        mainPane.getScene().getWindow().sizeToScene();
    }

    public static void showDialog() {
        Platform.runLater(() -> {
            CryptoTokenVS  tokenType = CryptoTokenVS.valueOf(ContextVS.getInstance().getProperty(
                    ContextVS.CRYPTO_TOKEN, CryptoTokenVS.DNIe.toString()));
            if(tokenType != CryptoTokenVS.JKS_KEYSTORE) {
                showMessage(ResponseVS.SC_ERROR, ContextVS.getMessage("improperTokenMsg"));
                return;
            }
            if(!BrowserSessionService.getInstance().isConnected()) {
                Button connectionButton = new Button();
                connectionButton.setGraphic(Utils.getIcon(FontAwesomeIcons.CLOUD_UPLOAD));
                connectionButton.setText(ContextVS.getMessage("connectLbl"));
                connectionButton.setOnAction(event -> {
                    WebSocketAuthenticatedService.getInstance().setConnectionEnabled(true);
                });
                showMessage(ContextVS.getMessage("authenticatedWebSocketConnectionRequiredMsg"), connectionButton);
                return;
            }
            try {
                if (INSTANCE == null) {
                    INSTANCE = new QRTransactionFormDialog();
                }
                INSTANCE.selectedTag = null;
                INSTANCE.toggleView(true);
                INSTANCE.show(ContextVS.getMessage("createQRLbl"));
            } catch (Exception ex) {
                log.log(Level.SEVERE, ex.getMessage(), ex);
            }
        });
    }

    @Override public void addTagVS(String tagName) {
        log.info("addTagVS: " + tagName);
        this.selectedTag = tagName;
        addTagButton.setText(ContextVS.getMessage("removeTagLbl"));
        tagLbl.setText(tagName);
        formVBox.getChildren().add(3, timeLimitedCheckBox);
    }

}