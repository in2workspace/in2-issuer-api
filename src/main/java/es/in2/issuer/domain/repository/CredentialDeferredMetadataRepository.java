package es.in2.issuer.domain.repository;

import es.in2.issuer.domain.entity.CredentialProcedure;
import es.in2.issuer.domain.entity.DeferredCredentialMetadata;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CredentialDeferredMetadataRepository extends ReactiveCrudRepository<DeferredCredentialMetadata, UUID> {
    Mono<CredentialProcedure> findByCredentialId (UUID credentialId);

    Mono<CredentialProcedure> findByTransactionId(String transactionId);
}
