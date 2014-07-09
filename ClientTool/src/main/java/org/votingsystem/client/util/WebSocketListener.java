package org.votingsystem.client.util;

import net.sf.json.JSONObject;

/**
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public interface WebSocketListener {

    public enum ConnectionStatus {OPEN, CLOSED}

    public void consumeWebSocketMessage(JSONObject messageJSON);
    public void setConnectionStatus(ConnectionStatus status);

}
