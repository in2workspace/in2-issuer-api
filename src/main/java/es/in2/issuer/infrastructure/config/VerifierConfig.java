package es.in2.issuer.infrastructure.config;

import es.in2.issuer.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.infrastructure.config.properties.VerifierProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VerifierConfig {
    private final ConfigAdapter configAdapter;
    private final VerifierProperties verifierProperties;

    public VerifierConfig(ConfigAdapterFactory configAdapterFactory, VerifierProperties verifierProperties) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.verifierProperties = verifierProperties;
    }

    public String getVerifierDidKey() {
        return configAdapter.getConfiguration(verifierProperties.verifierDidKey());
    }

    public String getVerifierExternalDomain() {
        return configAdapter.getConfiguration(verifierProperties.externalDomain());
    }

    public String getCredentialSubjectKey() {
        return configAdapter.getConfiguration(verifierProperties.credentialSubjectKey());
    }

    public String getVerifierVc() {
        return configAdapter.getConfiguration(verifierProperties.vc());
    }

    public String getVerifierCryptoPrivateKey() {
        return configAdapter.getConfiguration(verifierProperties.crypto().privateKey());
    }

    public String getVerifierClientAssertionTokenCronUnit() {
        return configAdapter.getConfiguration(verifierProperties.clientAssertion().token().cronUnit());
    }

    public String getVerifierClientAssertionTokenExpiration() {
        return configAdapter.getConfiguration(verifierProperties.clientAssertion().token().expiration());
    }

    public String getVerifierWellKnownPath() {
        return configAdapter.getConfiguration(verifierProperties.paths().wellKnownPath());
    }
}
