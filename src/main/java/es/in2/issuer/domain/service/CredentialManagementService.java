package es.in2.issuer.domain.service;

import es.in2.issuer.domain.entity.Credential;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CredentialManagementService {
    Mono<String> commitCredential(String credential, String userId);
    Mono<Void> updateCredential(String credential, Long credentialId, String userId);
    Mono<String> updateTransactionId(String transactionId, String userId);
    Mono<Void> setToEmitted(String transactionId, String userId);
    Flux<Credential> getCredentials (String userId);
    Mono<Credential> getCredential (Long credentialId);
    Mono<Credential> getCredentialByTransactionId (String transactionId, String userId);
}
