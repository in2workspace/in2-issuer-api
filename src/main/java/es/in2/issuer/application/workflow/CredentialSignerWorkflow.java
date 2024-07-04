package es.in2.issuer.application.workflow;

import reactor.core.publisher.Mono;

public interface CredentialSignerWorkflow {
    Mono<Void> signCredential(String authorizationHeader, String procedureId);
}
