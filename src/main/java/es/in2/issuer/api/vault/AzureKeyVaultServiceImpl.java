package es.in2.issuer.api.vault;

import com.azure.security.keyvault.secrets.SecretClient;
import es.in2.issuer.api.exception.AzureConfigurationSettingException;
import es.in2.issuer.api.vault.AzureKeyVaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!local")
@Primary
public class AzureKeyVaultServiceImpl implements AzureKeyVaultService {

    private final SecretClient secretClient;

    @Override
    public Mono<String> getSecretByKey(String key) {
        return Mono.fromCallable(() -> {
                    try {
                        return secretClient.getSecret(key).getValue();
                    } catch (Exception e) {
                        log.error("Azure Configuration Setting ERROR --> " + e.getMessage());
                        throw new AzureConfigurationSettingException("Communication with AppConfiguration failed. KeyVault not available");
                    }
                })
                .doOnSuccess(voidValue -> log.info("Secret retrieved successfully"))
                .onErrorResume(Exception.class, Mono::error);
    }
}
