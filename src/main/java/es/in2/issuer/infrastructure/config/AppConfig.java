package es.in2.issuer.infrastructure.config;

import es.in2.issuer.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.infrastructure.config.properties.ApiProperties;
import es.in2.issuer.infrastructure.config.properties.IssuerUiProperties;
import es.in2.issuer.infrastructure.config.properties.KnowledgebaseProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    private final ConfigAdapter configAdapter;
    private final ApiProperties apiProperties;
    private final IssuerUiProperties issuerUiProperties;
    private final KnowledgebaseProperties knowledgebaseProperties;

    public AppConfig(ConfigAdapterFactory configAdapterFactory, ApiProperties apiProperties, IssuerUiProperties issuerUiProperties, KnowledgebaseProperties knowledgebaseProperties) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.apiProperties = apiProperties;
        this.issuerUiProperties = issuerUiProperties;
        this.knowledgebaseProperties = knowledgebaseProperties;
    }

    public String getIssuerApiExternalDomain() {
        return configAdapter.getConfiguration(apiProperties.externalDomain());
    }

    public String getIssuerUiExternalDomain() {
        return configAdapter.getConfiguration(issuerUiProperties.externalDomain());
    }

    public String getKnowledgebaseUrl() {
        return configAdapter.getConfiguration(knowledgebaseProperties.url());
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

}
