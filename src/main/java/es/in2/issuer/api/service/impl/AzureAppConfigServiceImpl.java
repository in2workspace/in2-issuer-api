package es.in2.issuer.api.service.impl;

import com.azure.data.appconfiguration.ConfigurationClient;
import es.in2.issuer.api.config.azure.AzureAppConfigProperties;
import es.in2.issuer.api.service.AppConfigService;
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
public class AzureAppConfigServiceImpl implements AppConfigService {

    private final ConfigurationClient azureConfigurationClient;
    private final AzureAppConfigProperties azureAppConfigProperties;

    @Override
    public Mono<String> getConfiguration(String key) {
        return Mono.fromCallable(() -> {
                    try {
                        return azureConfigurationClient
                                .getConfigurationSetting(key, azureAppConfigProperties.azureConfigLabel)
                                .getValue();
                    } catch (Exception e) {
                        return "Communication with AppConfiguration failed. Prefix or label not available" + e;
                    }
                })
                .doOnSuccess(voidValue -> log.info("Secret retrieved successfully"))
                .onErrorResume(Exception.class, Mono::error);
    }
}
