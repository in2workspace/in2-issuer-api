package es.in2.issuer.authserver.application.workflow;

import es.in2.issuer.shared.domain.model.dto.PreAuthCodeResponse;
import reactor.core.publisher.Mono;

public interface PreAuthCodeWorkflow {
    Mono<PreAuthCodeResponse> generatePreAuthCodeResponse();
}
