package es.in2.issuer.backend.backoffice.infrastructure.config;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import es.in2.issuer.backend.shared.infrastructure.config.properties.AzureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AzAppConfigurationConfig {

    private final AzureProperties azureProperties;

    @Bean
    public TokenCredential azureTokenCredential() {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        log.debug("Token Credential: {}", credential);
        return credential;
    }

    @Bean
    public ConfigurationClient azureConfigurationClient(TokenCredential azureTokenCredential) {
        log.debug("AZ Properties endpoint --> {}", azureProperties.endpoint());
        return new ConfigurationClientBuilder()
                .credential(azureTokenCredential)
                .endpoint(azureProperties.endpoint())
                .buildClient();
    }

}
