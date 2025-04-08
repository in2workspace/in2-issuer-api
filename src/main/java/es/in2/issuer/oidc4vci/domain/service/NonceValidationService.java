package es.in2.issuer.oidc4vci.domain.service;

import es.in2.issuer.shared.domain.model.dto.NonceValidationResponse;
import reactor.core.publisher.Mono;

public interface NonceValidationService {
    Mono<NonceValidationResponse> validate(String nonce);
}
