package es.in2.issuer.api.config.provider;

public interface GenericConfigAdapter {
    String getKeycloakDomain();
    String getIssuerDomain();
    String getAuthenticSourcesDomain();
    String getKeyVaultDomain();
    String getRemoteSignatureDomain();
    String getKeycloakDid();
    String getIssuerDid();
}
