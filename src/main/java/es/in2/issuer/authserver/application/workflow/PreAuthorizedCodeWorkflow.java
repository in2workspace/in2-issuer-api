package es.in2.issuer.authserver.application.workflow;

import es.in2.issuer.shared.domain.model.dto.PreAuthorizedCodeResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PreAuthorizedCodeWorkflow {
    Mono<PreAuthorizedCodeResponse> generatePreAuthorizedCodeResponse(Mono<UUID> credentialIdMono);
}
