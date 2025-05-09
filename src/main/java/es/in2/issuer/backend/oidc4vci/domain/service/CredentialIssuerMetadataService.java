package es.in2.issuer.backend.oidc4vci.domain.service;

import es.in2.issuer.backend.oidc4vci.domain.model.CredentialIssuerMetadata;
import reactor.core.publisher.Mono;

public interface CredentialIssuerMetadataService {
    Mono<CredentialIssuerMetadata> buildCredentialIssuerMetadata(String processId);
}
