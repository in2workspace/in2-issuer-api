package es.in2.issuer.api.config.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("!local")
public class AzureAppConfigurationConfig {

    private final AzureAppConfigProperties azureAppConfigProperties;

    @Bean
    public TokenCredential azureTokenCredential() {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        log.info("Token Credential: {}", credential);
        return credential;
    }

    @Bean
    public ConfigurationClient azureConfigurationClient(TokenCredential azureTokenCredential) {
        log.info("ENDPOINT --> {}", azureAppConfigProperties.getAzureConfigEndpoint());

        return new ConfigurationClientBuilder()
                .credential(azureTokenCredential)
                .endpoint(azureAppConfigProperties.getAzureConfigEndpoint())
                .buildClient();
    }
}
