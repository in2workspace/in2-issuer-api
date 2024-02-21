package es.in2.issuer.api.config;

import es.in2.issuer.api.config.provider.ConfigAdapterFactory;
import es.in2.issuer.api.config.provider.ConfigProvider;
import es.in2.issuer.api.config.provider.ConfigProviderImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final ConfigAdapterFactory configAdapterFactory;

    @Bean
    public ConfigProvider ConfigProviderImpl() {
        return new ConfigProviderImpl(configAdapterFactory);
    }
    
}
