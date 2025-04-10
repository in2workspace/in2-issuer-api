package es.in2.issuer.shared.infrastructure.config;

import es.in2.issuer.shared.infrastructure.config.properties.CorsProperties;
import es.in2.issuer.shared.infrastructure.config.properties.IssuerIdentityProperties;
import es.in2.issuer.shared.infrastructure.config.properties.IssuerUiProperties;
import es.in2.issuer.shared.infrastructure.config.properties.KnowledgeBaseProperties;
import es.in2.issuer.shared.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.shared.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.shared.infrastructure.config.properties.ApiProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AppConfig {
    private final ConfigAdapter configAdapter;
    private final ApiProperties apiProperties;
    private final KnowledgeBaseProperties knowledgeBaseProperties;
    private final IssuerUiProperties issuerUiProperties;
    private final IssuerIdentityProperties issuerIdentityProperties;
    private final CorsProperties corsProperties;

    public AppConfig(
            ConfigAdapterFactory configAdapterFactory,
            ApiProperties apiProperties,
            KnowledgeBaseProperties knowledgeBaseProperties,
            IssuerUiProperties issuerUiProperties,
            IssuerIdentityProperties issuerIdentityProperties,
            CorsProperties corsProperties
    ) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.apiProperties = apiProperties;
        this.knowledgeBaseProperties = knowledgeBaseProperties;
        this.issuerUiProperties = issuerUiProperties;
        this.issuerIdentityProperties = issuerIdentityProperties;
        this.corsProperties = corsProperties;
    }

    public String getIssuerApiExternalDomain() {
        return configAdapter.getConfiguration(apiProperties.externalDomain());
    }

    public String getKnowledgeBaseUploadCertificationGuideUrl() {
        return configAdapter.getConfiguration(knowledgeBaseProperties.uploadCertificationGuideUrl());
    }

    public String getKnowledgebaseWalletUrl() {
        return configAdapter.getConfiguration(knowledgeBaseProperties.walletUrl());
    }

    public String getIssuerUiExternalDomain() {
        return configAdapter.getConfiguration(issuerUiProperties.externalDomain());
    }

    public String getCryptoPrivateKey() {
        return issuerIdentityProperties.crypto().privateKey();
    }

    public String getCredentialSubjectDidKey() {
        return issuerIdentityProperties.credentialSubjectDidKey();
    }

    public String getClientAssertionExpiration() {
        return issuerIdentityProperties.clientAssertion().expiration();
    }

    public String getClientAssertionExpirationUnitTime() {
        return issuerIdentityProperties.clientAssertion().expirationUnitTime();
    }

    public String getJwtCredential() {
        return issuerIdentityProperties.jwtCredential();
    }

    public String getApiConfigSource() {
        return configAdapter.getConfiguration(apiProperties.configSource());
    }

    public List<String> getExternalCorsAllowedOrigins() {
        return corsProperties.externalAllowedOrigins();
    }

    public List<String> getDefaultCorsAllowedOrigins() {
        return corsProperties.defaultAllowedOrigins();
    }
}
