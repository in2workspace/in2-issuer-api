package es.in2.issuer.backend.shared.application.workflow;

import reactor.core.publisher.Mono;

// TODO: Move to OIDC4VCI package
public interface NonceValidationWorkflow {
    Mono<Boolean> isValid(Mono<String> nonce);
}
