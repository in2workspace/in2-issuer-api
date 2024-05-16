package es.in2.issuer.domain.repository;

import es.in2.issuer.domain.entity.CredentialManagement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CredentialManagementRepository extends ReactiveCrudRepository<CredentialManagement, UUID> {
    Flux<CredentialManagement> findByUserIdOrderByModifiedAtDesc(String userId, Pageable pageable);
    Flux<CredentialManagement> findByUserIdAndCredentialStatusOrderByModifiedAtDesc(String userId, String credentialStatus, Pageable pageable);
    Mono<CredentialManagement> findByIdAndUserId(UUID id, String userId);
    @Query("SELECT * FROM credentials.credential_management WHERE user_id = :userId AND credential_decoded LIKE '%' || :credentialDecoded || '%'")
    Mono<CredentialManagement> findByUserIdAndCredentialDecodedContains(@Param("userId") String userId, @Param("credentialDecoded") String credentialDecoded);
}
