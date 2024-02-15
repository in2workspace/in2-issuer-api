package es.in2.issuer.api.model;

public class CredentialResponseErrorCodes {
    public static final String INVALID_REQUEST = "invalid_request";
    public static final String INVALID_TOKEN = "invalid_token";
    public static final String UNSUPPORTED_CREDENTIAL_TYPE = "unsupported_credential_type";
    public static final String UNSUPPORTED_CREDENTIAL_FORMAT = "unsupported_credential_format";
    public static final String INVALID_OR_MISSING_PROOF = "invalid_or_missing_proof";
    public static final String EXPIRED_PRE_AUTHORIZED_CODE = "pre-authorized_code is expired or used";
    public static final String VC_TEMPLATE_DOES_NOT_EXIST = "vc_template_does_not_exist";
    public static final String VC_DOES_NOT_EXIST = "vc_does_not_exist";
    public static final String USER_DOES_NOT_EXIST = "user_does_not_exist";

    public static final String DEFAULT_ERROR = "An error occurred";
}

