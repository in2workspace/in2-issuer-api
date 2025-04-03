package es.in2.issuer.oidc4vci.application.workflow;

import es.in2.issuer.shared.domain.model.dto.PreAuthorizedCodeResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PreAuthorizedCodeWorkflow {
    Mono<PreAuthorizedCodeResponse> generatePreAuthorizedCode(Mono<UUID> credentialIdMono);
}
