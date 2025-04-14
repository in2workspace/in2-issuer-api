package es.in2.issuer.backend.infrastructure.config;

import es.in2.issuer.backend.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.backend.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.backend.infrastructure.config.properties.*;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AppConfig {

    private final ConfigAdapter configAdapter;
    private final BackendProperties backendProperties;
    private final IssuerFrontendProperties issuerFrontendProperties;
    private final IssuerIdentityProperties issuerIdentityProperties;
    private final KnowledgeBaseProperties knowledgeBaseProperties;
    private final CorsProperties corsProperties;

    public AppConfig(
                        ConfigAdapterFactory configAdapterFactory,
                        BackendProperties backendProperties,
                        IssuerFrontendProperties issuerFrontendProperties,
                        IssuerIdentityProperties issuerIdentityProperties,
                        KnowledgeBaseProperties knowledgeBaseProperties,
                        CorsProperties corsProperties
    ) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.backendProperties = backendProperties;
        this.issuerFrontendProperties = issuerFrontendProperties;
        this.issuerIdentityProperties = issuerIdentityProperties;
        this.knowledgeBaseProperties = knowledgeBaseProperties;
        this.corsProperties = corsProperties;
    }

    public String getIssuerApiExternalDomain() {
        return configAdapter.getConfiguration(backendProperties.url());
    }

    public String getIssuerUiExternalDomain() {
        return configAdapter.getConfiguration(issuerFrontendProperties.url());
    }

    public String getKnowledgebaseWalletUrl() {
        return configAdapter.getConfiguration(knowledgeBaseProperties.walletUrl());
    }

    public String getKnowledgeBaseUploadCertificationGuideUrl() {
        return configAdapter.getConfiguration(knowledgeBaseProperties.uploadCertificationGuideUrl());
    }

    public String getApiConfigSource() {
        return configAdapter.getConfiguration(backendProperties.configSource());
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

    public List<String> getExternalCorsAllowedOrigins() {
        return corsProperties.externalAllowedOrigins();
    }
    public List<String> getDefaultCorsAllowedOrigins() {
        return corsProperties.defaultAllowedOrigins();
    }

}
