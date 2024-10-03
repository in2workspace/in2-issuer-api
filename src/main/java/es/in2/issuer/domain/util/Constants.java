package es.in2.issuer.domain.util;

import java.util.List;

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
    public static final String CLIENT_CREDENTIALS_GRANT_TYPE_VALUE = "client_credentials";
    public static final String CLIENT_ASSERTION_TYPE_VALUE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:pre-authorized_code";
    public static final String CREDENTIAL_SUBJECT = "credentialSubject";
    public static final String MANDATE = "mandate";
    public static final String MANDATEE = "mandatee";
    public static final String LEAR_CREDENTIAL_JWT = "LEARCredentialJWT";
    public static final String LEAR_CREDENTIAL_CWT = "LEARCredentialCWT";
    public static final String LEAR_CREDENTIAL_EMPLOYEE = "LEARCredentialEmployee";
    public static final String SUPPORTED_PROOF_ALG = "ES256";
    public static final String SUPPORTED_PROOF_TYP = "openid4vci-proof+jwt";
    public static final String REQUEST_ERROR_MESSAGE = "Error processing the request";
    public static final String ISSUER = "issuer";
    public static final String VALID_FROM = "validFrom";
    public static final String ISSUANCE_DATE = "issuanceDate";
    public static final String EXPIRATION_DATE = "expirationDate";
    public static final String ID = "id";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String ENGLISH = "en";
    public static final String UTF_8 = "UTF-8";
    public static final String FROM_EMAIL = "digitalidentitysupport@dome-marketplace.eu";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static final String CONTENT_TYPE_URL_ENCODED_FORM = "application/x-www-form-urlencoded";
    public static final String DID_ELSI = "did:elsi:";
    public static final String DID_KEY = "did:key:";
    public static final List<String> CREDENTIAL_CONTEXT = List.of("https://www.w3.org/ns/credentials/v2","https://dome-marketplace.eu/2022/credentials/learcredential/v1");
    public static final String ASYNC = "A";
    public static final String SYNC = "S";
    public static final String VERIFIABLE_CERTIFICATION = "VerifiableCertification";
    public static final List<String> VERIFIABLE_CERTIFICATION_TYPE = List.of(VERIFIABLE_CERTIFICATION, VERIFIABLE_CREDENTIAL);
    public static final long MSB = 0x80L;
    public static final long MSBALL = 0xFFFFFF80L;
    public static final String WELL_KNOWN_VERIFIER_ENDPOINT_PATH = "/.well-known/openid-configuration";

}
