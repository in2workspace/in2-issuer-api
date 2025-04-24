package es.in2.issuer.shared.application.workflow;

import es.in2.issuer.shared.domain.model.dto.PendingCredentials;
import es.in2.issuer.shared.domain.model.dto.SignedCredentials;
import reactor.core.publisher.Mono;

public interface DeferredCredentialWorkflow {

    Mono<PendingCredentials> getPendingCredentialsByOrganizationId(String organizationId);

    Mono<Void> updateSignedCredentials(SignedCredentials signedCredentials);

}
