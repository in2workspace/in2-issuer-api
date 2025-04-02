package es.in2.issuer.backend.infrastructure.config;

import es.in2.issuer.backend.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.backend.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.backend.infrastructure.config.properties.*;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AppConfig {

    private final ConfigAdapter configAdapter;
    private final ApiProperties apiProperties;
    private final IssuerUiProperties issuerUiProperties;
    private final IssuerIdentityProperties issuerIdentityProperties;
    private final KnowledgeBaseProperties knowledgeBaseProperties;
    private final CorsProperties corsProperties;

    public AppConfig(
                        ConfigAdapterFactory configAdapterFactory,
                        ApiProperties apiProperties,
                        IssuerUiProperties issuerUiProperties,
                        IssuerIdentityProperties issuerIdentityProperties,
                        KnowledgeBaseProperties knowledgeBaseProperties,
                        CorsProperties corsProperties
    ) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.apiProperties = apiProperties;
        this.issuerUiProperties = issuerUiProperties;
        this.issuerIdentityProperties = issuerIdentityProperties;
        this.knowledgeBaseProperties = knowledgeBaseProperties;
        this.corsProperties = corsProperties;
    }

    public String getIssuerApiExternalDomain() {
        return configAdapter.getConfiguration(apiProperties.externalDomain());
    }

    public String getIssuerUiExternalDomain() {
        return configAdapter.getConfiguration(issuerUiProperties.externalDomain());
    }

    public String getKnowledgebaseWalletUrl() {
        return configAdapter.getConfiguration(knowledgeBaseProperties.walletUrl());
    }

    public String getKnowledgeBaseUploadCertificationGuideUrl() {
        return configAdapter.getConfiguration(knowledgeBaseProperties.uploadCertificationGuideUrl());
    }

    public String getApiConfigSource() {
        return configAdapter.getConfiguration(apiProperties.configSource());
    }

    public long getCacheLifetimeForCredentialOffer() {
        return apiProperties.cacheLifetime().credentialOffer();
    }

    public long getCacheLifetimeForVerifiableCredential() {
        return apiProperties.cacheLifetime().verifiableCredential();
    }

    public String getCredentialSubjectDidKey() {
        return issuerIdentityProperties.credentialSubjectDidKey();
    }

    public String getJwtCredential() {
        return issuerIdentityProperties.jwtCredential();
    }

    public String getCryptoPrivateKey() {
        return issuerIdentityProperties.crypto().privateKey();
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
