package es.in2.issuer.vault;

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import es.in2.issuer.api.config.AppConfiguration;
import es.in2.issuer.api.exception.AzureConfigurationSettingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
@RequiredArgsConstructor
@Profile("!local")
public class AzureKeyVaultConfig {

    private final AppConfiguration appConfiguration;

    @Bean
    public SecretClient secretClient(TokenCredential azureTokenCredential) throws AzureConfigurationSettingException {
        String keyVaultEndpoint = appConfiguration.getKeyVaultDomain();
        if (keyVaultEndpoint != null) {
            return new SecretClientBuilder()
                    .vaultUrl(keyVaultEndpoint)
                    .credential(azureTokenCredential)
                    .buildClient();
        } else {
            throw new AzureConfigurationSettingException("KeyVaultEndpoint is null");
        }
    }

}
