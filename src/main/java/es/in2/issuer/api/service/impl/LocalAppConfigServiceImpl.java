package es.in2.issuer.api.service.impl;

import es.in2.issuer.api.service.AppConfigService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@AllArgsConstructor
@Profile("local")
public class LocalAppConfigServiceImpl implements AppConfigService {

    private final Environment env;

    @Override
    public Mono<String> getConfiguration(String key) {
        if (!env.containsProperty(key) || env.getProperty(key) == null) {
            return Mono.error(new Exception("Property " + key + " not found"));
        }

        return Mono.just(Objects.requireNonNull(env.getProperty(key)));
    }
}
