package es.in2.issuer.domain.repository;

import es.in2.issuer.domain.entity.CredentialManagement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CredentialManagementRepository extends ReactiveCrudRepository<CredentialManagement, UUID> {
    Flux<CredentialManagement> findByUserIdOrderByModifiedAtDesc(String userId, Pageable pageable);
    Mono<CredentialManagement> findByIdAndUserId(UUID id, String userId);
}
