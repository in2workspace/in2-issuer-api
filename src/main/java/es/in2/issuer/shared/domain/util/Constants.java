package es.in2.issuer.shared.domain.util;

public final class Constants {
    public static final String LEAR_CREDENTIAL = "LEARCredential";
    public static final String VERIFIABLE_CREDENTIAL = "VerifiableCredential";
    public static final String JWT_VC_JSON = "jwt_vc_json";
    public static final String LEAR_CREDENTIAL_EMPLOYEE = "LEARCredentialEmployee";
    public static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:pre-authorized_code";

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final long PRE_AUTH_CODE_EXPIRY_DURATION_MINUTES = 5;
    public static final String ENGLISH = "en";
}
