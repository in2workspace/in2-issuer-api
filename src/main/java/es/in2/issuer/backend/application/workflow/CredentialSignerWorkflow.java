package es.in2.issuer.backend.application.workflow;

import reactor.core.publisher.Mono;

public interface CredentialSignerWorkflow {
    Mono<String> signAndUpdateCredentialByProcedureId(String authorizationHeader, String procedureId, String format);
}
