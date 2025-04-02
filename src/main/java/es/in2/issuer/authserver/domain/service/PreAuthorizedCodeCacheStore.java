package es.in2.issuer.authserver.domain.service;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PreAuthorizedCodeCacheStore {

    Mono<String> save(String processId, String preAuthorizeCode, UUID credentialId, String txCode);
}
