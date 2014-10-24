package org.votingsystem.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
/**
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
@Entity
@Table(name="VicketServer")
@DiscriminatorValue("VicketServer")
public class VicketServer extends ActorVS implements Serializable {

    public static final long serialVersionUID = 1L;

    public VicketServer() {}

    public VicketServer(ActorVS actorVS) {
        setName(actorVS.getName());
        setX509Certificate(actorVS.getX509Certificate());
        setControlCenters(actorVS.getControlCenters());
        setEnvironmentVS(actorVS.getEnvironmentVS());
        setServerURL(actorVS.getServerURL());
        setState(actorVS.getState());
        setId(actorVS.getId());
        setTimeStampCert(actorVS.getTimeStampCert());
        setTrustAnchors(actorVS.getTrustAnchors());
    }

    @Override public Type getType() {
        return Type.VICKETS;
    }

    public String getTransactionVSServiceURL() {
        return getServerURL() + "/transactionVS";
    }

    public String getVicketRequestServiceURL() {
        return getServerURL() + "/vicket/request";
    }

    public String getVicketStatusServiceURL(String hashCertVS) {
        return getServerURL() + "/vicket/status/" +DatatypeConverter.printHexBinary(hashCertVS.getBytes());
    }

    public String getVicketTransactionServiceURL() {
        return getServerURL() + "/transactionVS/vicket";
    }

    public String getUserProceduresPageURL() {
        return getServerURL() + "/app/user?menu=user";
    }

    public String getAdminProceduresPageURL() {
        return getServerURL() + "/app/admin?menu=admin";
    }

    public String getSaveBankServiceURL() {
        return getServerURL() + "/userVS/newBankVS";
    }

    public String getSaveGroupVSServiceURL() {
        return getServerURL() + "/groupVS/newGroup";
    }

    public String getGroupVSSubscriptionServiceURL(Long groupId) {
        return getServerURL() + "/groupVS/" + String.valueOf(groupId) + "/subscribe";
    }

    public String getWalletURL() {
        return getServerURL() + "/vicket/wallet";
    }

    public String getGroupVSUsersServiceURL(Long groupId, Integer max, Integer offset,
                SubscriptionVS.State subscriptionState, UserVS.State userVSState) {
        return getServerURL() + "/groupVS/" + String.valueOf(groupId) + "/users" +
                "?max=" + ((max != null)?max:"") +
                "&offset=" + ((offset != null)?offset:"") +
                "&subscriptionState=" + ((subscriptionState != null)?subscriptionState.toString():"") +
                "&userVSState=" + ((userVSState != null)?userVSState.toString():"");
    }

    public String getGroupVSUsersActivationServiceURL() {
        return getServerURL() + "/groupVS/activateUser";
    }

}
