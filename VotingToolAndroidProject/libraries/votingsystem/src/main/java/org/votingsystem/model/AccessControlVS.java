package org.votingsystem.model;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.votingsystem.signature.util.CertUtil;
import org.votingsystem.util.DateUtils;
import org.votingsystem.util.StringUtils;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
* @author jgzornoza
* Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
*/
public class AccessControlVS extends ActorVS implements Serializable {

    public static final long serialVersionUID = 1L;
    
    private EventVS eventVS;
    private Set<ControlCenterVS> controlCenter;

    public AccessControlVS() {}

    public AccessControlVS(ActorVS actorVS) {
        setCertificate(actorVS.getCertificate());
        setCertChain(actorVS.getCertChain());
        setCertificatePEM(actorVS.getCertificatePEM());
        setCertificateURL(actorVS.getCertificateURL());
        setDateCreated(actorVS.getDateCreated());
        setId(actorVS.getId());
        setState(actorVS.getState());
        setServerURL(actorVS.getServerURL());
        setLastUpdated(actorVS.getLastUpdated());
        setName(actorVS.getName());
    }

    public void setEventVS(EventVS eventVS) {
        this.eventVS = eventVS;
    }

    public EventVS getEventVS() {
        return eventVS;
    }
        
    @Override
    public Type getType() {
        return Type.CONTROL_CENTER;
    }

    /**
     * @return the controlCenter
     */
    public Set<ControlCenterVS> getControlCenters() {
        return controlCenter;
    }

    /**
     * @param controlCenter the controlCenter to set
     */
    public void setControlCenters(Set<ControlCenterVS> controlCenter) {
        this.controlCenter = controlCenter;
    }


    public String getEventVSManifestCollectorURL (Long manifestId) {
        return getServerURL() + "/eventVSManifestCollector/" + manifestId;
    }

    public String getEventVSManifestURL (Long manifestId) {
        return getServerURL() + "/eventVSManifest/" + manifestId;
    }

    public String getCancelVoteServiceURL() {
        return getServerURL() + "/voteVSCanceller";
    }

    public String getEventVSClaimCollectorURL () {
        return getServerURL() + "/eventVSClaimCollector";
    }

    public String getSearchServiceURL (int offset, int max) {
        return getServerURL() + "/search/find?max=" + max + "&offset=" + offset;
    }

    public String getEventVSURL () {
        return getServerURL() + "/eventVS";
    }

    public String getServerInfoURL () {
        return getServerURL() + "/serverInfo";
    }

    public static String getServerInfoURL (String serverURL) {
        return StringUtils.checkURL(serverURL) + "/serverInfo";
    }

    public String getEventVSURL (EventVSState eventVSState, SubSystemVS subSystemVS,
            int max, int offset) {
        String subSystemPath = null;
        switch (subSystemVS) {
            case CLAIMS: subSystemPath = "/eventVSClaim";
                break;
            case MANIFESTS: subSystemPath = "/eventVSManifest";
                break;
            case VOTING: subSystemPath = "/eventVSElection";
                break;
            default: subSystemPath = "/eventVS";
                break;
        }
        String statePath = null;
        switch(eventVSState) {
            case CLOSED: statePath = "TERMINATED";
                break;
            case OPEN: statePath = "ACTIVE";
                break;
            case PENDING: statePath = "AWAITING";
                break;
            default: statePath = "";
                break;
        }
        return getServerURL() + subSystemPath + "?max="+ max + "&offset=" + offset  +
                "&eventVSState=" + statePath;
    }


    public String getPublishServiceURL(TypeVS formType) {
        switch(formType) {
            case CLAIM_PUBLISHING:return getServerURL() + "/mobileEditor/eventVSClaim";
            case MANIFEST_PUBLISHING:return getServerURL() + "/mobileEditor/eventVSManifest";
            case VOTING_PUBLISHING:return getServerURL() + "/mobileEditor/eventVSElection";
        }
        return null;
    }

    public String getUserCSRServiceURL (Long csrRequestId) {
        return getServerURL() + "/csr?csrRequestId=" + csrRequestId;
    }

    public String getUserCSRServiceURL () {
        return getServerURL() + "/csr/request";
    }

    public String getCertificationCentersURL () {
        return getServerURL() + "/serverInfo/certificationCenters";
    }


    public String getAccessServiceURL () {
        return getServerURL() +  "/accessRequestVS";
    }

    public String getTimeStampServiceURL () {
        return getServerURL() + "/timeStampVS";
    }

    public static AccessControlVS parse(String actorVSStr) throws Exception {
        JSONObject actorVSJSON = new JSONObject(actorVSStr);
        JSONObject jsonObject = null;
        JSONArray jsonArray;
        AccessControlVS actorVS = new AccessControlVS();
        if (actorVSJSON.getJSONArray("controlCenters") != null) {
            Set<ControlCenterVS> controlCenters = new HashSet<ControlCenterVS>();
            jsonArray = actorVSJSON.getJSONArray("controlCenters");
            for (int i = 0; i< jsonArray.length(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                ControlCenterVS controlCenter = new ControlCenterVS();
                controlCenter.setName(jsonObject.getString("name"));
                controlCenter.setServerURL(jsonObject.getString("serverURL"));
                controlCenter.setId(jsonObject.getLong("id"));
                controlCenter.setDateCreated(DateUtils.getDateFromString(jsonObject.getString("dateCreated")));
                if (jsonObject.getString("state") != null) {
                    controlCenter.setState(State.valueOf(jsonObject.getString("state")));
                }
                controlCenters.add(controlCenter);
            }
            ((AccessControlVS)actorVS).setControlCenters(controlCenters);
        }
        if (actorVSJSON.has("urlBlog"))
            actorVS.setUrlBlog(actorVSJSON.getString("urlBlog"));
        if (actorVSJSON.has("serverURL"))
            actorVS.setServerURL(actorVSJSON.getString("serverURL"));
        if (actorVSJSON.has("name"))
            actorVS.setName(actorVSJSON.getString("name"));
        if (actorVSJSON.has("certChainPEM")) {
            Collection<X509Certificate> certChain =
                    CertUtil.fromPEMToX509CertCollection(actorVSJSON.
                            getString("certChainPEM").getBytes());
            actorVS.setCertChain(certChain);
            X509Certificate serverCert = certChain.iterator().next();
            Log.d(TAG + ".getActorConIP(..) ", " - actorVS Cert: "
                    + serverCert.getSubjectDN().toString());
            actorVS.setCertificate(serverCert);
        }
        if (actorVSJSON.has("timeStampCertPEM")) {
            actorVS.setTimeStampCertPEM(actorVSJSON.getString(
                    "timeStampCertPEM"));
        }
        return actorVS;
    }

}