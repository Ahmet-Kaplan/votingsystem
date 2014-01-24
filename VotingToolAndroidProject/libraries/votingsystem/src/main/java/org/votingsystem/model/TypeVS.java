package org.votingsystem.model;

/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
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

    TICKET,
    TICKET_REQUEST,
    TICKET_REQUEST_DIALOG,
    TICKET_USER_INFO,

    RECEIPT;
}
