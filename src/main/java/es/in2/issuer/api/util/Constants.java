package es.in2.issuer.api.util;

public class Constants {
    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String LEAR_CREDENTIAL = "LEARCredential";
    public static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:pre-authorized_code";
    public static final String CREDENTIAL_SUBJECT = "credentialSubject";
}