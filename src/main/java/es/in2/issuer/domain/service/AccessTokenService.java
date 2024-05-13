package es.in2.issuer.domain.service;

import reactor.core.publisher.Mono;

public interface AccessTokenService {
    Mono<String> getCleanBearerToken(String authorizationHeader);
    Mono<String> getUserIdFromHeader(String authorizationHeader);
}
