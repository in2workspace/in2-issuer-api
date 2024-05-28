package es.in2.issuer.application.workflow;

import es.in2.issuer.domain.model.dto.PendingCredentials;
import es.in2.issuer.domain.model.dto.SignedCredentials;
import reactor.core.publisher.Mono;

public interface CredentialManagementWorkflow {
    Mono<PendingCredentials> getPendingCredentialsByOrganizationId(String organizationId);
    Mono<Void> updateSignedCredentials(SignedCredentials signedCredentials);
}
