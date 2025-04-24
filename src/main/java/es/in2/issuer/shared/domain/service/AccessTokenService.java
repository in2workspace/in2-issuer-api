package es.in2.issuer.shared.domain.service;

import reactor.core.publisher.Mono;

public interface AccessTokenService {
    Mono<String> getCleanBearerToken(String authorizationHeader);
    Mono<String> getUserId(String authorizationHeader);
    Mono<String> getOrganizationId(String authorizationHeader);
    Mono<String> getOrganizationIdFromCurrentSession();

}
