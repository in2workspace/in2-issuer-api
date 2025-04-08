package es.in2.issuer.backoffice.infrastructure.config;

import es.in2.issuer.shared.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.shared.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.backoffice.infrastructure.config.properties.*;
import es.in2.issuer.shared.infrastructure.config.properties.ApiProperties;
import es.in2.issuer.shared.infrastructure.config.properties.IssuerIdentityProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AppConfig {

    private final ConfigAdapter configAdapter;
    private final ApiProperties apiProperties;
    private final IssuerIdentityProperties issuerIdentityProperties;
    private final CorsProperties corsProperties;

    public AppConfig(
                        ConfigAdapterFactory configAdapterFactory,
                        ApiProperties apiProperties,
                        IssuerIdentityProperties issuerIdentityProperties,
                        CorsProperties corsProperties
    ) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.apiProperties = apiProperties;
        this.issuerIdentityProperties = issuerIdentityProperties;
        this.corsProperties = corsProperties;
    }

    public String getApiConfigSource() {
        return configAdapter.getConfiguration(apiProperties.configSource());
    }

    public String getCredentialSubjectDidKey() {
        return issuerIdentityProperties.credentialSubjectDidKey();
    }

    public String getJwtCredential() {
        return issuerIdentityProperties.jwtCredential();
    }

    public String getClientAssertionExpiration() {
        return issuerIdentityProperties.clientAssertion().expiration();
    }

    public String getClientAssertionExpirationUnitTime() {
        return issuerIdentityProperties.clientAssertion().expirationUnitTime();
    }

    public List<String> getExternalCorsAllowedOrigins() {
        return corsProperties.externalAllowedOrigins();
    }
    public List<String> getDefaultCorsAllowedOrigins() {
        return corsProperties.defaultAllowedOrigins();
    }

}
