package es.in2.issuer.oidc4vci.domain.service;

import es.in2.issuer.oidc4vci.domain.model.dto.AuthorizationServerMetadata;
import reactor.core.publisher.Mono;

public interface AuthorizationServerMetadataService {
    Mono<AuthorizationServerMetadata> generateOpenIdAuthorizationServerMetadata();
}
