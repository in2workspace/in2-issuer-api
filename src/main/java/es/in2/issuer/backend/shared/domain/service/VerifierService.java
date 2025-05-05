package es.in2.issuer.backend.shared.domain.service;

import es.in2.issuer.backend.shared.domain.model.dto.OpenIDProviderMetadata;
import es.in2.issuer.backend.shared.domain.model.dto.VerifierOauth2AccessToken;
import reactor.core.publisher.Mono;

public interface VerifierService {

    Mono<Void> verifyToken(String accessToken);
    Mono<Void> verifyTokenWithoutExpiration(String accessToken);
    Mono<OpenIDProviderMetadata> getWellKnownInfo();
    Mono<VerifierOauth2AccessToken> performTokenRequest(String body);
}
