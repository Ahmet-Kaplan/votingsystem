package org.votingsystem.client.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.votingsystem.dto.OperationVS;

import java.util.Map;

/**
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public interface BrowserVS {

    public void invokeBrowserCallback(Object dto, String callerCallback) throws JsonProcessingException;
    public void processOperationVS(OperationVS operationVS, String passwordDialogMessage);
    public void processOperationVS(String password, OperationVS operationVS);
    public void processSignalVS(Map signalData);
}
