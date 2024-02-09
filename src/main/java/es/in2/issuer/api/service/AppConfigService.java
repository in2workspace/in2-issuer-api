package es.in2.issuer.api.service;

import reactor.core.publisher.Mono;

public interface AppConfigService {
    Mono<String> getConfiguration(String key);
}
