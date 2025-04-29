package es.in2.issuer.backend.backoffice.application.workflow;

import es.in2.issuer.backend.shared.domain.model.dto.PreSubmittedCredentialRequest;
import reactor.core.publisher.Mono;

public interface CredentialActionsWorkflow {
    Mono<Void> sendReminder(String processId, String recordId);
    Mono<Void> signCredential(String processId, String recordId, String authorizationHeader);
    Mono<Void> revokeCredential(String processId, String authorizationHeader);
    Mono<Void> renewCredential(String processId, String recordId, String authorizationHeader);
}
