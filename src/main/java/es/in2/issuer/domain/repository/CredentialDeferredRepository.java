package es.in2.issuer.domain.repository;

import es.in2.issuer.domain.entity.CredentialDeferred;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CredentialDeferredRepository extends ReactiveCrudRepository<CredentialDeferred, UUID> {
    Mono<CredentialDeferred> findByCredentialId (UUID credentialId);

    Mono<CredentialDeferred> findByTransactionId(String transactionId);
}
