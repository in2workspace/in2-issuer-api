package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import reactor.core.publisher.Mono;

public interface M2MTokenService {

    Mono<VerifierOauth2AccessToken> getM2MToken();
    Mono<Void> verifyM2MToken(VerifierOauth2AccessToken token);
}
