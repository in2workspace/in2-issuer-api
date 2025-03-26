package es.in2.issuer.authserver.domain.service;

import reactor.core.publisher.Mono;

public interface PreAuthCodeCacheStore {

    Mono<String> save(String processId, String preAuthorizeCode, String pin);
}
