package es.in2.issuer.backend.oidc4vci.application.workflow;

import es.in2.issuer.backend.oidc4vci.domain.model.AuthorizationServerMetadata;
import reactor.core.publisher.Mono;

public interface GetAuthorizationServerMetadataWorkflow {
    Mono<AuthorizationServerMetadata> execute(String processId);
}
