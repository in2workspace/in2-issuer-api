package es.in2.issuer.shared.infrastructure.config.security.service;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Mono;

public interface PolicyAuthorizationService {
    Mono<Void> authorize(String authorizationHeader, String schema, JsonNode payload);
}
