package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.dto.UserDetails;
import reactor.core.publisher.Mono;

public interface AccessTokenService {
    Mono<String> getCleanBearerToken(String authorizationHeader);
    Mono<String> getUserId(String authorizationHeader);
    Mono<String> getOrganizationId(String authorizationHeader);
    Mono<String> getOrganizationIdFromCurrentSession();
    Mono<UserDetails> getUserDetailsFromCurrentSession();
}
