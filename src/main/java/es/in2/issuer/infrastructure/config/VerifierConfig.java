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

    public String getVerifierExternalDomain() {
        return configAdapter.getConfiguration(verifierProperties.externalDomain());
    }

    public String getVerifierInternalDomain() {
        return configAdapter.getConfiguration(verifierProperties.internalDomain());
    }

    public String getVerifierKey() {
        return configAdapter.getConfiguration(verifierProperties.verifierKey());
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
        return configAdapter.getConfiguration(String.valueOf(verifierProperties.clientAssertion().token().expiration()));
    }

    public String getVerifierPathsTokenPaths() {
        return configAdapter.getConfiguration(verifierProperties.paths().tokenPath());
    }


}
