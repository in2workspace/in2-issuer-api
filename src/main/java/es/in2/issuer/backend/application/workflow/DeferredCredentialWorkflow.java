package es.in2.issuer.backend.application.workflow;

import es.in2.issuer.backend.domain.model.dto.PendingCredentials;
import es.in2.issuer.backend.domain.model.dto.SignedCredentials;
import reactor.core.publisher.Mono;

public interface DeferredCredentialWorkflow {

    Mono<PendingCredentials> getPendingCredentialsByOrganizationId(String organizationId);

    Mono<Void> updateSignedCredentials(SignedCredentials signedCredentials);

}
