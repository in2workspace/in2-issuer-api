package es.in2.issuer.infrastructure.repository;

import es.in2.issuer.domain.model.entities.SignatureConfigurationAudit;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface SignatureConfigurationAuditRepository extends ReactiveCrudRepository<SignatureConfigurationAudit, UUID> {
    Flux<SignatureConfigurationAudit> findAllByOrganizationIdentifier(String organizationIdentifier);

}
