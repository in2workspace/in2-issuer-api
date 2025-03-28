package es.in2.issuer.authserver.application.workflow;

import es.in2.issuer.shared.domain.model.dto.PreAuthorizedCodeResponse;
import reactor.core.publisher.Mono;

public interface PreAuthorizedCodeWorkflow {
    Mono<PreAuthorizedCodeResponse> generatePreAuthCodeResponse();
}
