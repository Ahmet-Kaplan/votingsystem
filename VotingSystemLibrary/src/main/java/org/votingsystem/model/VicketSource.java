package org.votingsystem.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.security.cert.X509Certificate;

/**
 * @author jgzornoza
 * Licencia: https://github.com/votingsystem/votingsystem/wiki/Licencia
 */
@Entity @Table(name="VicketSource") @DiscriminatorValue("VicketSource")
public class VicketSource extends UserVS implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public void beforeInsert() {
        if(getType() == null) setType(Type.VICKET_SOURCE);
    }

    public static VicketSource getUserVS (X509Certificate certificate) {
        VicketSource userVS = new VicketSource();
        userVS.setCertificate(certificate);
        String subjectDN = certificate.getSubjectDN().getName();
        if (subjectDN.contains("C=")) userVS.setCountry(subjectDN.split("C=")[1].split(",")[0]);
        if (subjectDN.contains("SERIALNUMBER=")) userVS.setNif(subjectDN.split("SERIALNUMBER=")[1].split(",")[0]);
        if (subjectDN.contains("SURNAME=")) userVS.setLastName(subjectDN.split("SURNAME=")[1].split(",")[0]);
        if (subjectDN.contains("GIVENNAME=")) {
            String givenname = subjectDN.split("GIVENNAME=")[1].split(",")[0];
            userVS.setName(givenname);
            userVS.setFirstName(givenname);
        }
        if (subjectDN.contains("CN=")) userVS.setCn(subjectDN.split("CN=")[1]);
        return userVS;
    }
}
