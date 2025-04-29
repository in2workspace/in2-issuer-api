package es.in2.issuer.backend.backoffice.application.workflow.impl;

import es.in2.issuer.backend.backoffice.application.workflow.CredentialActionsWorkflow;
import es.in2.issuer.backend.backoffice.domain.service.NotificationService;
import es.in2.issuer.backend.shared.domain.model.dto.PreSubmittedCredentialRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CredentialActionsWorkflowImpl implements CredentialActionsWorkflow {

    private final NotificationService notificationService;

    @Override
    public Mono<Void> sendReminder(String processId, String recordId) {
        return notificationService.sendNotification(processId, recordId);
    }

    @Override
    public Mono<Void> signCredential(String processId, String recordId, String authorizationHeader) {
        // Implementation for signing a credential
        return Mono.empty();
    }

    @Override
    public Mono<Void> revokeCredential(String processId, String authorizationHeader) {
        // Implementation for revoking a credential
        return Mono.empty();
    }

    @Override
    public Mono<Void> renewCredential(String processId, String recordId, String authorizationHeader) {
        // Implementation for renewing a credential
        return Mono.empty();
    }

}
