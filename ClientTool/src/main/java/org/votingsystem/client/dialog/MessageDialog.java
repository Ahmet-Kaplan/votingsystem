package org.votingsystem.client.dialog;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;
import org.votingsystem.client.util.Utils;
import org.votingsystem.model.ContextVS;
import org.votingsystem.model.ResponseVS;

/**
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class MessageDialog {

    private static Logger logger = Logger.getLogger(MessageDialog.class);

    private final Stage stage;
    private HBox messageBox;
    private Label messageLabel;

    public MessageDialog() {
        stage = new Stage(StageStyle.TRANSPARENT);
        stage.initModality(Modality.WINDOW_MODAL);
        //stage.initOwner(window);

        stage.addEventHandler(WindowEvent.WINDOW_SHOWN, new EventHandler<WindowEvent>() {
            @Override public void handle(WindowEvent window) {      }
        });

        VBox mainBox = new VBox(10);

        messageBox = new HBox(10);
        messageLabel = new Label();
        messageLabel.setWrapText(true);

        messageBox.getChildren().add(messageLabel);

        Button acceptButton = new Button(ContextVS.getMessage("acceptLbl"));
        acceptButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent actionEvent) {
                stage.hide();
            }});

        HBox footerButtonsBox = new HBox(10);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        footerButtonsBox.getChildren().addAll(spacer, acceptButton);

        mainBox.getChildren().addAll(messageLabel, footerButtonsBox);
        mainBox.getStyleClass().add("modal-dialog");
        stage.setScene(new Scene(mainBox, Color.TRANSPARENT));
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
    }

    public void showMessage(String message) {
        messageLabel.setText(message);
        messageLabel.setGraphic(null);
        stage.centerOnScreen();
        stage.show();
        stage.toFront();
    }

    public void showMessage(int statusCode, String message) {
        messageLabel.setText(message);
        if(ResponseVS.SC_OK == statusCode) {
            messageLabel.setGraphic( new ImageView(Utils.getImage(MessageDialog.this, "accept_32")));
        } else messageLabel.setGraphic( new ImageView(Utils.getImage(MessageDialog.this, "cancel_32")));
        stage.centerOnScreen();
        stage.show();
        stage.toFront();
    }

    class Delta { double x, y; }

}