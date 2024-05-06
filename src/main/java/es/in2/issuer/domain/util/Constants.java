package es.in2.issuer.domain.util;

public class Constants {

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String LEAR_CREDENTIAL = "LEARCredential";
    public static final String VERIFIABLE_CREDENTIAL = "VerifiableCredential";
    public static final String JWT_VC = "jwt_vc_json";
    public static final String CWT_VC = "cwt_vc";
    public static final String JWT_VC_JSON = "jwt_vc_json";
    public static final String CWT_VC_JSON = "cwt_vc_json";
    public static final String PRE_AUTHORIZATION_CODE = "pre-authorization_code";
    public static final String AUTHORIZATION_CODE = "authorization_code";

    public static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:pre-authorized_code";
    public static final String CREDENTIAL_SUBJECT = "credentialSubject";

    public static final String LEAR_CREDENTIAL_JWT = "LEARCredentialJWT";
    public static final String LEAR_CREDENTIAL_CWT = "LEARCredentialCWT";

    public static final String SUPPORTED_PROOF_ALG = "ES256";
    public static final String SUPPORTED_PROOF_TYP = "openid4vci-proof+jwt";
    public static final String REQUEST_ERROR_MESSAGE = "Error processing the request";
    public static final String ISSUER = "issuer";
    public static final String VALID_FROM = "validFrom";
    public static final String ISSUANCE_DATE = "issuanceDate";
    public static final String EXPIRATION_DATE = "expirationDate";
    public static final String ID = "id";

}
