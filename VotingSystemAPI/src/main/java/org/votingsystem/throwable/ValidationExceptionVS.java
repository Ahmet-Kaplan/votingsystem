package org.votingsystem.throwable;

import org.votingsystem.dto.MessageDto;

/**
 * License: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
public class ValidationExceptionVS extends ExceptionVS {

    public ValidationExceptionVS(MessageDto messageDto) {
        super(messageDto);
    }

    public ValidationExceptionVS(String message) {
        super(message);
    }

    public ValidationExceptionVS(String message, String metaInf) {
        super(message, metaInf);
    }

    public ValidationExceptionVS(String message, String metaInf, Throwable cause) {
        super(message, metaInf, cause);
    }

    public ValidationExceptionVS(String message, Throwable cause) {
        super(message, cause);
    }

}
