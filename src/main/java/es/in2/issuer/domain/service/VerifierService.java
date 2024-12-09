package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.dto.OpenIDProviderMetadata;
import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import reactor.core.publisher.Mono;

public interface VerifierService {

    Mono<Void> verifyToken(String accessToken);
    Mono<OpenIDProviderMetadata> getWellKnownInfo();
    Mono<VerifierOauth2AccessToken> performTokenRequest(String body);
}
