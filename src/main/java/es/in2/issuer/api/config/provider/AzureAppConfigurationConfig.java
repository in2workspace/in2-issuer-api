package es.in2.issuer.api.config.provider;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import es.in2.issuer.api.config.provider.properties.AzureProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@RequiredArgsConstructor
@Slf4j
@ConfigSourceName(name = ConfigProviderNameEnum.AZURE)
public class AzureAppConfigurationConfig {

    private final AzureProperties azureProperties;

    @Bean
    public TokenCredential azureTokenCredential() {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        log.info("Token Credential: {}", credential);
        return credential;
    }

    @Bean
    public ConfigurationClient azureConfigurationClient(TokenCredential azureTokenCredential) {
        log.info("ENDPOINT --> {}", azureProperties.endpoint());

        return new ConfigurationClientBuilder()
                .credential(azureTokenCredential)
                .endpoint(azureProperties.endpoint())
                .buildClient();
    }
}
