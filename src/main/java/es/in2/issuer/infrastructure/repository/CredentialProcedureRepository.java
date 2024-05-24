package es.in2.issuer.infrastructure.repository;

import es.in2.issuer.domain.model.entities.CredentialProcedure;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CredentialProcedureRepository extends ReactiveCrudRepository<CredentialProcedure, UUID> {
}
