package es.in2.issuer.backend.oidc4vci.application.workflow;

import es.in2.issuer.backend.shared.domain.model.dto.PreAuthorizedCodeResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PreAuthorizedCodeWorkflow {
    Mono<PreAuthorizedCodeResponse> generatePreAuthorizedCode(Mono<UUID> credentialIdMono);
}
