package es.in2.issuer.backend.shared.infrastructure.repository;

import es.in2.issuer.backend.shared.domain.model.entities.DeferredCredentialMetadata;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface DeferredCredentialMetadataRepository extends ReactiveCrudRepository<DeferredCredentialMetadata, UUID> {

    Mono<DeferredCredentialMetadata> findByTransactionId(String transactionId);
    Mono<DeferredCredentialMetadata> findByTransactionCode(String transactionCode);
    Mono<DeferredCredentialMetadata> findByAuthServerNonce(String authServerNonce);
    Mono<DeferredCredentialMetadata> findByProcedureId(UUID procedureId);
    Mono<Void> deleteByAuthServerNonce(String deleteByAuthServerNonce);
}
