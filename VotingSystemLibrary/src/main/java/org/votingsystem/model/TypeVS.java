package org.votingsystem.model;

/**
* @author jgzornoza
* Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
*/
public enum TypeVS {

    ALERT,
	CONTROL_CENTER_VALIDATED_VOTE,
    ACCESS_CONTROL_VALIDATED_VOTE,
	   
    REQUEST_WITH_ERRORS,
    REQUEST_WITHOUT_FILE,
    EVENT_WITH_ERRORS,
    OK,
    EXCEPTION,
    FORMAT_DATE,
    TEST,
    SIGNATURE_ERROR,
    ERROR,
    CANCELLED,
    USER_ERROR,
    RECEIPT,
    RECEIPT_VIEW,
    VOTING_EVENT,
    VOTEVS,
    VOTE_ERROR,
    CANCEL_VOTE,
    CANCEL_VOTE_ERROR,
    CONTROL_CENTER_ASSOCIATION,
    CONNECT,
    DISCONNECT,
    BACKUP,
    MANIFEST_EVENT,
    MANIFEST_EVENT_ERROR,
    CLAIM_EVENT_SIGN,
    CLAIM_EVENT, 
    CLAIM_EVENT_ERROR,
    CLAIM_EVENT_SIGNATURE_ERROR,
    VOTE_RECEIPT_ERROR,
    RECEIPT_ERROR,
    VOTE_RECEIPT,
    INDEX_REQUEST,
    SELECT_IMAGE,
    SIGNAL_VS,
    INDEX_REQUEST_ERROR,
    ACCESS_REQUEST_ERROR,
    SMIME_CLAIM_SIGNATURE,
    SEND_SMIME_VOTE,
	REPRESENTATIVE_SELECTION,

    ANONYMOUS_REPRESENTATIVE_REQUEST,
    ANONYMOUS_REPRESENTATIVE_SELECTION,
    ANONYMOUS_REPRESENTATIVE_SELECTION_CANCELLED,

    BACKUP_REQUEST, 
    MANIFEST_PUBLISHING, 
    MANIFEST_SIGN, 
    CLAIM_PUBLISHING,
    VOTING_PUBLISHING,
    ACCESS_REQUEST,
    ACCESS_REQUEST_CANCELLATION,
    EVENT_CANCELLATION,
    KEYSTORE_SELECT,
    SAVE_SMIME,
    SAVE_SMIME_ANONYMOUS_DELEGATION,
    OPEN_SMIME,
    OPEN_SMIME_FROM_URL,
    OPEN_COOIN,
    NEW_REPRESENTATIVE,
    REPRESENTATIVE_VOTING_HISTORY_REQUEST,
    REPRESENTATIVE_VOTING_HISTORY_REQUEST_ERROR,
    REPRESENTATIVE_REVOKE_ERROR,
    REPRESENTATIVE_ACCREDITATIONS_REQUEST,
    REPRESENTATIVE_ACCREDITATIONS_REQUEST_ERROR,
    REPRESENTATIVE_DATA_ERROR,
    REPRESENTATIVE_DATA,
    REPRESENTATIVE_REVOKE,
    REPRESENTATIVE_STATE,
    TERMINATED,

    MESSAGEVS,
    MESSAGEVS_SIGN,
    MESSAGEVS_SIGN_RESPONSE,
    MESSAGEVS_TO_DEVICE,
    MESSAGEVS_FROM_DEVICE,
    MESSAGEVS_FROM_VS,

    LISTEN_TRANSACTIONS,
    INIT_VALIDATED_SESSION,

    CERT_USER_NEW,
    CERT_CA_NEW,
    CERT_EDIT,

    FILE_FROM_URL,

    FROM_BANKVS,
    FROM_GROUP_TO_MEMBER,
    FROM_GROUP_TO_MEMBER_GROUP,
    FROM_GROUP_TO_ALL_MEMBERS,
    FROM_USERVS,

    COOIN,
    COOIN_USER_INFO,
    COOIN_CANCEL,
    COOIN_DELETE,
    COOIN_REQUEST,
    COOIN_SEND,
    COOIN_USERVS_CHANGE,
    COOIN_WALLET_CHANGE,
    COOIN_GROUP_NEW,
    COOIN_GROUP_EDIT,
    COOIN_GROUP_CANCEL,
    COOIN_GROUP_SUBSCRIBE,
    COOIN_GROUP_UPDATE_SUBSCRIPTION,
    COOIN_GROUP_USER_ACTIVATE,
    COOIN_GROUP_USER_DEACTIVATE,
    COOIN_INIT_PERIOD,
    BANKVS,
    BANKVS_NEW,

    PAYMENT,
    PAYMENT_REQUEST,
    DELIVERY_WITHOUT_PAYMENT,
    DELIVERY_WITH_PAYMENT,
    REQUEST_FORM,

    WEB_SOCKET_INIT,
    WEB_SOCKET_BAN_SESSION,

    WALLET_OPEN,
    WALLET_SAVE,
    WALLET_STATE, COOIN_IMPORT;

}
