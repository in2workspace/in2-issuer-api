package es.in2.issuer.backoffice.domain.service;

import es.in2.issuer.backoffice.domain.model.dto.ProcedureBasicInfo;
import es.in2.issuer.backoffice.domain.model.dto.PendingCredentials;
import es.in2.issuer.backoffice.domain.model.dto.SignedCredentials;
import es.in2.issuer.backoffice.domain.model.entities.CredentialProcedure;
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

    Flux<ProcedureBasicInfo> getCredentials(String userId, int page, int size, String sort, Sort.Direction direction);

    Mono<PendingCredentials> getPendingCredentials(String userId, int page, int size, String sort, Sort.Direction direction);

    Mono<ProcedureBasicInfo> getCredential(UUID credentialId, String userId);

    Mono<CredentialProcedure> getDeferredCredentialByTransactionId(String transactionId);
}
