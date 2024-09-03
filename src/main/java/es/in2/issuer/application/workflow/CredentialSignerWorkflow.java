package es.in2.issuer.application.workflow;

import reactor.core.publisher.Mono;

public interface CredentialSignerWorkflow {
    Mono<String> signAndUpdateCredential(String authorizationHeader, String procedureId);
    Mono<String> signCredentialOnRequestedFormat(String unsignedCredential, String format, String token);
}
