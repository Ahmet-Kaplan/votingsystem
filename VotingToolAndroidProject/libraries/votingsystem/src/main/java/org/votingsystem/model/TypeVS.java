package org.votingsystem.model;

/**
* @author jgzornoza
* Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
*/
public enum TypeVS {

    ACCESS_REQUEST,
    ITEM_REQUEST,
    NIF_REQUEST,
    ITEMS_REQUEST,
    VOTING_EVENT,
    VOTEVS,
    VOTEVS_CANCELLED,
    CANCEL_VOTE,

    REPRESENTATIVE_REVOKE,
    NEW_REPRESENTATIVE,
    REPRESENTATIVE,
    ANONYMOUS_REPRESENTATIVE_SELECTION,
    ANONYMOUS_REPRESENTATIVE_REQUEST,
    ANONYMOUS_REPRESENTATIVE_SELECTION_CANCELLED,
    REPRESENTATIVE_SELECTION,

    PIN,
    PIN_CHANGE,
    EVENT_CANCELLATION,
    BACKUP_REQUEST,
    SEND_SMIME_VOTE,
    VOTING_PUBLISHING,

    COOIN,
    COOIN_CANCEL,
    FROM_USERVS,
    COOIN_GROUP_NEW,
    COOIN_REQUEST,
    COOIN_TICKET_REQUEST,
    COOIN_SEND,
    USERVS_MONETARY_INFO,

    STATE,
    TRANSACTIONVS,

    MESSAGEVS,
    MESSAGEVS_GET,
    MESSAGEVS_EDIT,
    MESSAGEVS_SIGN,
    MESSAGEVS_DECRYPT,
    MESSAGEVS_TO_DEVICE,
    MESSAGEVS_FROM_DEVICE,

    LISTEN_TRANSACTIONS,
    INIT_VALIDATED_SESSION,
    WEB_SOCKET_INIT,
    WEB_SOCKET_CLOSE,
    WEB_SOCKET_MESSAGE,
    WEB_SOCKET_BAN_SESSION,

    RECEIPT;
}
