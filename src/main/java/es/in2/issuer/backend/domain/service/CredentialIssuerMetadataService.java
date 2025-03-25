package es.in2.issuer.backend.domain.service;

import es.in2.issuer.backend.domain.model.dto.CredentialIssuerMetadata;
import reactor.core.publisher.Mono;

public interface CredentialIssuerMetadataService {
    Mono<CredentialIssuerMetadata> generateOpenIdCredentialIssuer();
}
