package es.in2.issuer.api.service;

import reactor.core.publisher.Mono;

public interface AzureKeyVaultService {
    Mono<String> getSecretByKey(String key);
}
