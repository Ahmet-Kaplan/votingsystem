package org.bouncycastle2.asn1.smime;

import org.bouncycastle2.asn1.DERObjectIdentifier;
import org.bouncycastle2.asn1.pkcs.PKCSObjectIdentifiers;

public interface SMIMEAttributes
{
    public static final DERObjectIdentifier  smimeCapabilities = PKCSObjectIdentifiers.pkcs_9_at_smimeCapabilities;
    public static final DERObjectIdentifier  encrypKeyPref = PKCSObjectIdentifiers.id_aa_encrypKeyPref;
}