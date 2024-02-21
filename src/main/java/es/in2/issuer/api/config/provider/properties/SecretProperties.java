package es.in2.issuer.api.config.provider.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.secrets")
@Validated
public record SecretProperties(
        @NotNull String keycloakDomain,
        @NotNull String issuerDomain,
        @NotNull String authenticSourcesDomain,
        @NotNull String keyVaultDomain,
        @NotNull String remoteSignatureDomain,
        @NotNull String keycloakDid,
        @NotNull String issuerDid
) {
    @ConstructorBinding
    public SecretProperties(String keycloakDomain, String issuerDomain, String authenticSourcesDomain, String keyVaultDomain, String remoteSignatureDomain, String keycloakDid, String issuerDid) {
        this.keycloakDomain = keycloakDomain;
        this.issuerDomain = issuerDomain;
        this.authenticSourcesDomain = authenticSourcesDomain;
        this.keyVaultDomain = keyVaultDomain;
        this.remoteSignatureDomain = remoteSignatureDomain;
        this.keycloakDid = keycloakDid;
        this.issuerDid = issuerDid;
    }
}
