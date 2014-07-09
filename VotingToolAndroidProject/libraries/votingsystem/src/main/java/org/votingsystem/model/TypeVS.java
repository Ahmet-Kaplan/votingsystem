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
    REPRESENTATIVE_SELECTION,

    CONTROL_CENTER_ASSOCIATION,
    MANIFEST_EVENT,
    CLAIM_EVENT,

    PIN,
    EVENT_CANCELLATION,
    BACKUP_REQUEST,
    SEND_SMIME_VOTE,
    MANIFEST_SIGN,
    SMIME_CLAIM_SIGNATURE,
    CLAIM_PUBLISHING,
    MANIFEST_PUBLISHING,
    VOTING_PUBLISHING,

    VICKET,
    VICKET_REQUEST,
    VICKET_SEND,
    VICKET_USER_INFO,
    VICKET_CANCEL,
    VICKET_GROUP_NEW,

    TRANSACTION,

    USER_ALLOCATION_INPUT,
    USER_ALLOCATION_INPUT_RECEIPT,

    MESSAGEVS,
    MESSAGEVS_GET,
    MESSAGEVS_EDIT,
    MESSAGEVS_DECRYPT,

    LISTEN_TRANSACTIONS,
    INIT_VALIDATED_SESSION,

    WEB_SOCKET_INIT,
    WEB_SOCKET_CLOSE,
    WEB_SOCKET_MESSAGE,
    WEB_SOCKET_ADD_SESSION,

    RECEIPT;
}
