package es.in2.issuer.domain.service;

import es.in2.issuer.domain.entity.CredentialDeferred;
import es.in2.issuer.domain.model.CredentialItem;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CredentialManagementService {
    Mono<String> commitCredential(String credential, String userId, String format);
    Mono<Void> updateCredential(String credential, UUID credentialId, String userId);
    Mono<String> updateTransactionId(String transactionId);
    Mono<Void> deleteCredentialDeferred(String transactionId);
    Flux<CredentialItem> getCredentials (String userId, int page, int size, String sort, Sort.Direction direction);
    Mono<CredentialItem> getCredential (UUID credentialId, String userId);
    Mono<CredentialDeferred> getDeferredCredentialByTransactionId (String transactionId);
}