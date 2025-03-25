package es.in2.issuer.backend.domain.service;

import es.in2.issuer.backend.domain.model.dto.VerifierOauth2AccessToken;
import reactor.core.publisher.Mono;

public interface M2MTokenService {
    Mono<VerifierOauth2AccessToken> getM2MToken();
}
