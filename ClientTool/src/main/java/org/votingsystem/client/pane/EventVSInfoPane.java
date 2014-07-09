package org.votingsystem.client.pane;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebView;
import org.apache.log4j.Logger;
import org.votingsystem.client.model.MetaInf;
import org.votingsystem.client.util.Utils;
import org.votingsystem.model.ContextVS;
import org.votingsystem.model.TypeVS;
import org.votingsystem.util.DateUtils;

import java.io.File;

/**
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class EventVSInfoPane extends GridPane {

    private static Logger logger = Logger.getLogger(EventVSInfoPane.class);

    private MetaInf metaInf = null;


    public EventVSInfoPane(final MetaInf metaInf) throws Exception {
        this.metaInf = metaInf;
        Label subjectLabel = new Label(ContextVS.getMessage("subjectLbl") + ": ");
        subjectLabel.setStyle("-fx-font-weight: bold;");
        add(subjectLabel, 0, 0);
        Label subjectValueLabel = new Label(metaInf.getSubject());
        add(subjectValueLabel, 1, 0);

        Label dateInitLabel = new Label(ContextVS.getMessage("dateInitLbl") + ": ");
        dateInitLabel.setStyle("-fx-font-weight: bold;");
        add(dateInitLabel, 0 , 1);
        Label dateInitValueLabel = new Label(DateUtils.getShortStringFromDate(metaInf.getDateInit()));
        add(dateInitValueLabel, 1, 1);

        Label dateFinishLabel = new Label(ContextVS.getMessage("dateFinishLbl") + ": ");
        dateFinishLabel.setStyle("-fx-font-weight: bold;");
        add(dateFinishLabel, 0, 2);
        Label dateFinishValueLabel = new Label(DateUtils.getShortStringFromDate(metaInf.getDateFinish()));
        add(dateFinishValueLabel, 1, 2);

        WebView webView = new WebView();
        webView.getEngine().setUserDataDirectory(new File(ContextVS.WEBVIEWDIR));
        webView.getEngine().loadContent(metaInf.getFormattedInfo());
        webView.setPrefHeight(300);
        add(webView, 0, 3, 2, 1);

        if(metaInf.getType() == TypeVS.VOTING_EVENT) {
            Button representativesButton = new Button(ContextVS.getMessage("representativesDetailsLbl"));
            representativesButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent actionEvent) {
                    HTMLMessageDialog messageDialog = new HTMLMessageDialog();
                    messageDialog.showMessage(metaInf.getRepresentativesHTML(), ContextVS.getMessage("representativesDetailsLbl"));
                }});
            representativesButton.setGraphic(new ImageView(Utils.getImage(this, "group")));
        }
    }

}
