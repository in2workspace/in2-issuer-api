package es.in2.issuer.domain.repository;

import es.in2.issuer.domain.entity.CredentialProcedure;
import es.in2.issuer.domain.entity.DeferredCredentialMetadata;
import org.checkerframework.checker.units.qual.C;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface CredentialProcedureRepository extends ReactiveCrudRepository<CredentialProcedure, UUID> {
    Flux<CredentialProcedure> findByUserIdOrderByModifiedAtDesc(String userId, Pageable pageable);
}
