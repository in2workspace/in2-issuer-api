package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.dto.CredentialIssuerMetadata;
import reactor.core.publisher.Mono;

public interface CredentialIssuerMetadataService {
    Mono<CredentialIssuerMetadata> generateOpenIdCredentialIssuer();
}
