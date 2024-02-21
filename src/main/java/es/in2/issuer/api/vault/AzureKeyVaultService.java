package es.in2.issuer.api.vault;

import reactor.core.publisher.Mono;

public interface AzureKeyVaultService {
    Mono<String> getSecretByKey(String key);
}
