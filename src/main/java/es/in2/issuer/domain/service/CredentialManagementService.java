package es.in2.issuer.domain.service;

import es.in2.issuer.domain.entity.CredentialProcedure;
import es.in2.issuer.domain.model.CredentialItem;
import es.in2.issuer.domain.model.PendingCredentials;
import es.in2.issuer.domain.model.SignedCredentials;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CredentialManagementService {
    Mono<String> commitCredential(String credential, String userId, String format);
    Mono<Void> updateCredential(String credential, UUID credentialId, String userId);
    Mono<Void> updateCredentials(SignedCredentials signedCredentials, String userId);
    Mono<String> updateTransactionId(String transactionId);
    Mono<Void> deleteCredentialDeferred(String transactionId);
    Flux<CredentialItem> getCredentials (String userId, int page, int size, String sort, Sort.Direction direction);
    Mono<PendingCredentials> getPendingCredentials(String userId, int page, int size, String sort, Sort.Direction direction);
    Mono<CredentialItem> getCredential (UUID credentialId, String userId);
    Mono<CredentialProcedure> getDeferredCredentialByTransactionId (String transactionId);
}
