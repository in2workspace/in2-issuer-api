package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.AppNonceValidationResponse;
import es.in2.issuer.domain.model.NonceResponse;
import reactor.core.publisher.Mono;

public interface NonceManagementService {
    Mono<NonceResponse> saveAccessTokenAndNonce(AppNonceValidationResponse appNonceValidationResponse);
    Mono<String> getTokenFromCache(String nonce);
}
