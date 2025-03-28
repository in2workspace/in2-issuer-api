package es.in2.issuer.authserver.domain.service;

import es.in2.issuer.shared.domain.model.dto.PreAuthorizedCodeResponse;
import reactor.core.publisher.Mono;

public interface PreAuthCodeService {
    Mono<PreAuthorizedCodeResponse> generatePreAuthCodeResponse(String processId);
}
