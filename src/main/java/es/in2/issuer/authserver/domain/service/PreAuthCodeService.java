package es.in2.issuer.authserver.domain.service;

import es.in2.issuer.shared.domain.model.dto.PreAuthCodeResponse;
import reactor.core.publisher.Mono;

public interface PreAuthCodeService {
    Mono<PreAuthCodeResponse> generatePreAuthCodeResponse(String processId);
}
