package es.in2.issuer.vault;

import reactor.core.publisher.Mono;

public interface AzureKeyVaultService {
    Mono<String> getSecretByKey(String key);
}
