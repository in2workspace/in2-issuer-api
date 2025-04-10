package es.in2.issuer.application.workflow;

import reactor.core.publisher.Mono;

public interface CredentialSignerWorkflow {
    Mono<String> signAndUpdateCredentialByProcedureId(String authorizationHeader, String procedureId, String format);
    Mono<Void> retrySignUnsignedCredential(String authorizationHeader, String procedureId);
}
