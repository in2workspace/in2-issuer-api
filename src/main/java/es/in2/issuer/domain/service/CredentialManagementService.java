package es.in2.issuer.domain.service;

import es.in2.issuer.domain.entity.Credential;
import es.in2.issuer.domain.entity.CredentialDeferred;
import es.in2.issuer.domain.entity.CredentialListItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public interface CredentialManagementService {
    Mono<String> commitCredential(String credential, String userId);
    Mono<Void> updateCredential(String credential, UUID credentialId, String userId);
    Mono<String> updateTransactionId(String transactionId);
    Mono<Void> setToEmitted(String transactionId, String userId);
    Mono<Void> deleteCredentialDeferred(String transactionId);
    Flux<CredentialListItem> getCredentials (String userId, int page, int size, String sort, Sort.Direction direction);
    Mono<Credential> getCredential (UUID credentialId, String userId);
    Mono<CredentialDeferred> getDeferredCredentialByTransactionId (String transactionId);
}
