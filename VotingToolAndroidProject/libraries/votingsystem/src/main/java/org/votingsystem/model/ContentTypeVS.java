package org.votingsystem.model;

/**
 * @author jgzornoza
 * Licencia: https://github.com/jgzornoza/SistemaVotacion/wiki/Licencia
 *
 * S/MIME signatures are usually "detached signatures": the signature information is separate
 * from the text being signed. The MIME type for this is multipart/signed with the second part
 * having a MIME subtype of application/(x-)pkcs7-signature
 */
public enum ContentTypeVS {

    BACKUP("application/backup", "zip"),
    JAVASCRIPT("application/javascript", "js"),
    JSON("application/json", "json"),
    MULTIPART_SIGNED("multipart/signed", null),
    MULTIPART_ENCRYPTED("multipart/encrypted", null),
    PDF("application/pdf", "pdf"),
    PDF_SIGNED_AND_ENCRYPTED("application/pdf;application/x-pkcs7-signature;application/x-pkcs7-mime", "pdf"),
    PDF_SIGNED("application/pdf;application/x-pkcs7-signature", "pdf"),
    PDF_ENCRYPTED("application/pdf;application/x-pkcs7-mime", "pdf"),
    TEXT("text/plain", "txt"),
    TIMESTAMP_QUERY("timestamp-query", null),
    ZIP("application/zip", "zip"),

    OCSP_REQUEST("application/ocsp-request", null),
    OCSP_RESPONSE("application/ocsp-response", null),

    SIGNED("application/x-pkcs7-signature","p7s"),
    ENCRYPTED("application/x-pkcs7-mime","p7m"),//application/x-pkcs7-mime p7c
    SIGNED_AND_ENCRYPTED("application/x-pkcs7-signature;application/x-pkcs7-mime", "p7m"),

    PKCS7_CERT("application/x-pkcs7-certificates","p7b"),//application/x-pkcs7-certificates	 spc
    PKCS7_CERT_REQ_RESP("application/x-pkcs7-certreqresp","p7r"),
    PKCS12("application/x-pkcs12","p12"),//application/x-pkcs12	pfx
    PKIX_CRL("application/pkix-crl", "crl"),
    PKCS10("application/pkcs10", "p10"),

    X509("application/x-x509-ca-cert", null);

    private String name;
    private String extension;

    private ContentTypeVS(String name, String extension) {
        this.name = name;
        this.extension = extension;
    }

    public String getName() {
        return name;
    }

    public String getExtension() {
        return extension;
    }

    public static ContentTypeVS getByName(String contentTypeStr) {
        if(contentTypeStr.contains(SIGNED_AND_ENCRYPTED.getName())) return SIGNED_AND_ENCRYPTED;
        if(contentTypeStr.contains(ENCRYPTED.getName())) return ENCRYPTED;
        if(contentTypeStr.contains(SIGNED.getName())) return SIGNED;

        if(contentTypeStr.contains(PDF_SIGNED_AND_ENCRYPTED.getName())) return PDF_SIGNED_AND_ENCRYPTED;
        if(contentTypeStr.contains(PDF_ENCRYPTED.getName())) return PDF_ENCRYPTED;
        if(contentTypeStr.contains(PDF_SIGNED.getName())) return PDF_SIGNED;
        if(contentTypeStr.contains(PDF.getName())) return PDF;

        if(contentTypeStr.contains(MULTIPART_ENCRYPTED.getName())) return MULTIPART_ENCRYPTED;
        if(contentTypeStr.contains(MULTIPART_SIGNED.getName())) return MULTIPART_SIGNED;

        return null;
    }

    public static ContentTypeVS getByExtension(String extensionStr) {
        if(extensionStr.contains(PDF.getExtension())) return PDF;
        if(extensionStr.contains(ZIP.getExtension())) return ZIP;
        if(extensionStr.contains(TEXT.getExtension())) return TEXT;
        if(extensionStr.contains(JSON.getExtension())) return JSON;
        if(extensionStr.contains(JAVASCRIPT.getExtension())) return JAVASCRIPT;
        return null;
    }

}
