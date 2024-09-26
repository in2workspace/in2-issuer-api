package es.in2.issuer.domain.service;

import com.nimbusds.oauth2.sdk.TokenRequest;
import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import reactor.core.publisher.Mono;

public interface AccessTokenService {
    Mono<String> getCleanBearerToken(String authorizationHeader);
    Mono<String> getUserId(String authorizationHeader);
    Mono<String> getOrganizationId(String authorizationHeader);
    Mono<String> getOrganizationIdFromCurrentSession();
    Mono<VerifierOauth2AccessToken> getM2MToken();

}
