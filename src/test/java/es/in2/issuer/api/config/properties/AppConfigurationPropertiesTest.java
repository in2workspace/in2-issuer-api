package es.in2.issuer.api.config.properties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class AppConfigurationPropertiesTest {
    @Test
    void bindingProperties() {
        // Setup a MockEnvironment with your properties
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("keycloakExternalDomain", "example.com");
        environment.setProperty("issuerDomain", "issuer.example.com");
        environment.setProperty("authenticSourcesDomain", "authsources.example.com");
        environment.setProperty("keyVaultDomain", "keyvault.example.com");
        environment.setProperty("remoteSignatureDomain", "remotesignature.example.com");
        environment.setProperty("keycloakDid", "keycloak-did");
        environment.setProperty("issuerDid", "issuer-did");

        // Create a Binder instance
        Binder binder = Binder.get(environment);

        // Bind the properties to an AppConfigurationProperties instance
        BindResult<AppConfigurationProperties> bindResult =
                binder.bind("", AppConfigurationProperties.class);

        // Assert the properties are correctly bound
        assertThat(bindResult.isBound()).isTrue();
        AppConfigurationProperties properties = bindResult.get();
        assertThat(properties.keycloakExternalDomain()).isEqualTo("example.com");
        assertThat(properties.issuerDomain()).isEqualTo("issuer.example.com");
        assertThat(properties.authenticSourcesDomain()).isEqualTo("authsources.example.com");
        assertThat(properties.keyVaultDomain()).isEqualTo("keyvault.example.com");
        assertThat(properties.remoteSignatureDomain()).isEqualTo("remotesignature.example.com");
        assertThat(properties.keycloakDid()).isEqualTo("keycloak-did");
        assertThat(properties.issuerDid()).isEqualTo("issuer-did");
    }
}