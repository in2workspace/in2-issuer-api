package es.in2.issuer.backend.oidc4vci.domain.service;

import es.in2.issuer.backend.oidc4vci.domain.model.AuthorizationServerMetadata;
import reactor.core.publisher.Mono;

public interface AuthorizationServerMetadataService {
    Mono<AuthorizationServerMetadata> buildAuthorizationServerMetadata(String processId);
}
