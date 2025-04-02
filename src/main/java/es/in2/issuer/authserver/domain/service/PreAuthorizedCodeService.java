package es.in2.issuer.authserver.domain.service;

import es.in2.issuer.shared.domain.model.dto.PreAuthorizedCodeResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PreAuthorizedCodeService {
    Mono<PreAuthorizedCodeResponse> generatePreAuthorizedCodeResponse(String processId, UUID credentialId);
}
