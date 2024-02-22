package es.in2.issuer.api.config.properties;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app.configs")
@Validated
public record AppConfigurationProperties(
        @NotNull String keycloakDomain,
        @NotNull String issuerDomain,
        @NotNull String authenticSourcesDomain,
        @NotNull String keyVaultDomain,
        @NotNull String remoteSignatureDomain,
        @NotNull String keycloakDid,
        @NotNull String issuerDid
) {
    @ConstructorBinding
    public AppConfigurationProperties(String keycloakDomain, String issuerDomain, String authenticSourcesDomain, String keyVaultDomain, String remoteSignatureDomain, String keycloakDid, String issuerDid) {
        this.keycloakDomain = keycloakDomain;
        this.issuerDomain = issuerDomain;
        this.authenticSourcesDomain = authenticSourcesDomain;
        this.keyVaultDomain = keyVaultDomain;
        this.remoteSignatureDomain = remoteSignatureDomain;
        this.keycloakDid = keycloakDid;
        this.issuerDid = issuerDid;
    }
}
