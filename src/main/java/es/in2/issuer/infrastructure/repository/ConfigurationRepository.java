package es.in2.issuer.infrastructure.repository;

import es.in2.issuer.domain.model.entities.Configuration;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ConfigurationRepository extends ReactiveCrudRepository<Configuration, UUID> {

    Flux<Configuration> findAllByOrganizationIdentifier(String organizationIdentifier);

}
