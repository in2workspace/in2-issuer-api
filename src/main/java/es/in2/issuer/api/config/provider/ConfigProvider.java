package es.in2.issuer.api.config.provider;

public interface ConfigProvider {
    String getBaseUrl();
    String getKeycloakDomain();
    String getIssuerDomain();
    String getAuthenticSourcesDomain();
    String getKeyVaultDomain();
    String getRemoteSignatureDomain();
    String getKeycloakDid();
    String getIssuerDid();
}
