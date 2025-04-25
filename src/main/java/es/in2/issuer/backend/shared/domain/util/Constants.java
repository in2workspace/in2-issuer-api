package es.in2.issuer.backend.shared.domain.util;

import java.util.List;

import static es.in2.issuer.backend.backoffice.domain.util.Constants.VERIFIABLE_CERTIFICATION;

public final class Constants {
    public static final String LEAR_CREDENTIAL = "LEARCredential";
    public static final String VERIFIABLE_CREDENTIAL = "VerifiableCredential";
    public static final List<String> VERIFIABLE_CERTIFICATION_TYPE = List.of(VERIFIABLE_CERTIFICATION, VERIFIABLE_CREDENTIAL);
    public static final String JWT_VC_JSON = "jwt_vc_json";
    public static final String LEAR_CREDENTIAL_EMPLOYEE = "LEARCredentialEmployee";
    public static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:pre-authorized_code";
    public static final List<String> CREDENTIAL_CONTEXT = List.of("https://www.w3.org/ns/credentials/v2", "https://www.dome-marketplace.eu/2025/credentials/learcredentialemployee/v2");
    // EXPIRATION TIMES
    public static final Integer CREDENTIAL_OFFER_CACHE_EXPIRATION_TIME = 10;
    public static final Integer VERIFIABLE_CREDENTIAL_JWT_CACHE_EXPIRATION_TIME = 10;
    public static final Integer CLIENT_ASSERTION_EXPIRATION_TIME = 2;
    public static final String CLIENT_ASSERTION_EXPIRATION_TIME_UNIT = "MINUTES";

    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final long PRE_AUTH_CODE_EXPIRY_DURATION_MINUTES = 5;
    public static final String ENGLISH = "en";
}
