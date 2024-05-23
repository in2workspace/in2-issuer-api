package es.in2.issuer.infrastructure.repository;

import es.in2.issuer.domain.entity.DeferredCredentialMetadata;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface DeferredCredentialMetadataRepository extends ReactiveCrudRepository<DeferredCredentialMetadata, UUID> {

    Mono<DeferredCredentialMetadata> findByTransactionId(String transactionId);
    Mono<DeferredCredentialMetadata> findByTransactionCode(String transactionCode);
    Mono<DeferredCredentialMetadata> findByAuthServerNonce(String authServerNonce);
}
