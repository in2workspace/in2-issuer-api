package es.in2.issuer.api.service;

import es.in2.issuer.api.model.dto.CredentialIssuerMetadata;
import reactor.core.publisher.Mono;

public interface CredentialIssuerMetadataService {
    Mono<CredentialIssuerMetadata> generateOpenIdCredentialIssuer();
}
