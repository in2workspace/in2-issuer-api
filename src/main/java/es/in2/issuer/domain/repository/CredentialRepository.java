package es.in2.issuer.domain.repository;

import es.in2.issuer.domain.entity.Credential;
import es.in2.issuer.domain.entity.CredentialListItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CredentialRepository extends ReactiveCrudRepository<Credential, UUID> {
    Flux<CredentialListItem> findByUserIdOrderByModifiedAtDesc(String userId, Pageable pageable);
    Mono<Credential> findByTransactionIdAndUserId(String transactionId, String userId);
    Mono<Credential> findByIdAndUserId(UUID id, String userId);
}