package es.in2.issuer.shared.infrastructure.config;

import es.in2.issuer.shared.infrastructure.config.properties.IssuerIdentityProperties;
import es.in2.issuer.shared.infrastructure.config.properties.IssuerUiProperties;
import es.in2.issuer.shared.infrastructure.config.properties.KnowledgeBaseProperties;
import es.in2.issuer.shared.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.shared.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.shared.infrastructure.config.properties.ApiProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    private final ConfigAdapter configAdapter;
    private final ApiProperties apiProperties;
    private final KnowledgeBaseProperties knowledgeBaseProperties;
    private final IssuerUiProperties issuerUiProperties;
    private final IssuerIdentityProperties issuerIdentityProperties;

    public AppConfig(
            ConfigAdapterFactory configAdapterFactory,
            ApiProperties apiProperties,
            KnowledgeBaseProperties knowledgeBaseProperties,
            IssuerUiProperties issuerUiProperties,
            IssuerIdentityProperties issuerIdentityProperties
    ) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.apiProperties = apiProperties;
        this.knowledgeBaseProperties = knowledgeBaseProperties;
        this.issuerUiProperties = issuerUiProperties;
        this.issuerIdentityProperties = issuerIdentityProperties;
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
}
