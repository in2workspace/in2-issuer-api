package es.in2.issuer.oidc4vci.domain.service;

import es.in2.issuer.oidc4vci.domain.model.dto.TokenResponse;
import reactor.core.publisher.Mono;

public interface TokenService {
    Mono<TokenResponse> generateTokenResponse(
            String grantType,
            String preAuthorizedCode,
            String txCode);
}
