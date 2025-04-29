package es.in2.issuer.backend.oidc4vci.application.workflow;

import es.in2.issuer.backend.oidc4vci.domain.model.CredentialIssuerMetadata;
import reactor.core.publisher.Mono;

public interface GetCredentialIssuerMetadataWorkflow {
    Mono<CredentialIssuerMetadata> execute(String processId);
}
