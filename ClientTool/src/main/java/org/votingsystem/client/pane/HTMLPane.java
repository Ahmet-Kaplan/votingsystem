package org.votingsystem.client.pane;

import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.votingsystem.client.util.Utils;
import org.votingsystem.util.ContextVS;

import java.io.File;
import java.util.logging.Logger;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class HTMLPane extends VBox{

    private static Logger log = Logger.getLogger(HTMLPane.class.getSimpleName());

    private WebView webView;

    public HTMLPane(String paneContent) {
        webView = new WebView();
        webView.getEngine().setUserDataDirectory(new File(ContextVS.WEBVIEWDIR));
        webView.getEngine().loadContent(paneContent);
        Utils.browserVSLinkListener(webView);
        VBox.setVgrow(webView, Priority.ALWAYS);
        getChildren().addAll(webView);
    }

}