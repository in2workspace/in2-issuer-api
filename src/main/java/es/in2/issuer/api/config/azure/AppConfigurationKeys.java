package es.in2.issuer.api.config.azure;

public final class AppConfigurationKeys {

    public static final String ISSUER_VCI_BASE_URL_KEY = "aca-issuer-vci-ms-uri";
    public static final String ISSUER_AUTHENTIC_SOURCES_BASE_URL_KEY = "aca-issuer-auth-src-ms-uri";
    public static final String CROSS_REMOTE_SIGNATURE_BASE_URL_KEY = "aca-cross-rmt-sign-ms-uri";
    public static final String KEYCLOAK_URI_KEY = "aca-cross-keycloak-ms-uri";

    // SECRETS

    public static final String KEY_VAULT_ENDPOINT_KEY = "key-vault-uri";
    public static final String DID_ISSUER_KEYCLOAK_SECRET = "issuer-keycloak-did";
    public static final String DID_ISSUER_INFO_ID_SECRET = "issuer-info-id-did";

}
