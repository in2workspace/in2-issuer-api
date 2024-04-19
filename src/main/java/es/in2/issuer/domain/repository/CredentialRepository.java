package es.in2.issuer.domain.repository;

import es.in2.issuer.domain.entity.Credential;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CredentialRepository extends ReactiveCrudRepository<Credential, Long> {
    Flux<Credential> findByUserIdOrderByModifiedAtDesc(String userId);
    Mono<Credential> findByTransactionIdAndUserId(String transactionId, String userId);
}