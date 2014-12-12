package org.votingsystem.client.pane;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;
import org.votingsystem.client.util.CooinStatusChecker;
import org.votingsystem.client.util.MsgUtils;
import org.votingsystem.client.util.Utils;
import org.votingsystem.cooin.model.Cooin;
import org.votingsystem.model.ContextVS;
import org.votingsystem.model.CooinServer;
import org.votingsystem.model.ResponseVS;
import org.votingsystem.util.HttpHelper;
import org.votingsystem.util.WebSocketMessage;

import java.io.IOException;

/**
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class InboxMessageRow implements CooinStatusChecker.Listener {

    private static Logger log = Logger.getLogger(InboxMessageRow.class);

    public interface Listener {
        public void removeMessage(WebSocketMessage webSocketMessage);
    }

    @FXML private HBox mainPane;
    @FXML private Label descriptionLbl;
    @FXML private Button messageButton;
    private WebSocketMessage webSocketMessage;
    private Listener listener;

    public InboxMessageRow(WebSocketMessage webSocketMessage, Listener listener) throws IOException {
        this.webSocketMessage = webSocketMessage;
        this.listener = listener;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/InboxMessageRow.fxml"));
        fxmlLoader.setController(this);
        fxmlLoader.load();
    }

    @FXML void initialize() { // This method is called by the FXMLLoader when initialization is complete
        log.debug("initialize");
        switch(webSocketMessage.getOperation()) {
            case COOIN_WALLET_CHANGE:
                messageButton.setText(ContextVS.getMessage("cooin_wallet_change_button"));
                descriptionLbl.setText(MsgUtils.getCooinChangeWalletMsg(webSocketMessage));
                new Thread(new CooinStatusChecker(webSocketMessage.getCooinList(), this)).start();
                break;
            default:
                descriptionLbl.setText(webSocketMessage.getOperation().toString());
                messageButton.setText(webSocketMessage.getOperation().toString());
        }

    }

    @Override public void processCooinStatus(Cooin cooin, Integer statusCode) {
        if(ResponseVS.SC_OK != statusCode) {
            log.debug("Cooin '" + cooin.getHashCertVS() + "' - statusCode: " + statusCode);
            listener.removeMessage(webSocketMessage);
        }
    }

    public void onClickMessageButton(ActionEvent actionEvent) {
        switch(webSocketMessage.getOperation()) {
            case COOIN_WALLET_CHANGE:
                descriptionLbl.setText(ContextVS.getMessage("cooin_wallet_change_button"));
                messageButton.setText(ContextVS.getMessage("cooin_wallet_change_button"));
                break;
            default:

        }
    }

    public HBox getMainPane() {
        return mainPane;
    }

}