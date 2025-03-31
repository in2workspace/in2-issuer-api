package es.in2.issuer.infrastructure.repository;

import es.in2.issuer.domain.model.entities.SignatureConfiguration;
import es.in2.issuer.domain.model.enums.SignatureMode;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SignatureConfigurationRepository extends ReactiveCrudRepository<SignatureConfiguration, UUID> {
    Mono<SignatureConfiguration> findBySecretRelativePath(String secretRelativePath);
    Flux<SignatureConfiguration> findAllByOrganizationIdentifier(String organizationIdentifier);
    Flux<SignatureConfiguration> findAllByOrganizationIdentifierAndSignatureMode(String organizationIdentifier, SignatureMode signatureMode);


}
