package es.in2.issuer.authserver.domain.service.impl;

import es.in2.issuer.authserver.domain.service.PreAuthCodeCacheStore;
import es.in2.issuer.shared.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PreAuthCodeCacheStoreImpl implements PreAuthCodeCacheStore {
    private final CacheStore<String> pinByPreAuthCodeCacheStore;

    @Override
    public Mono<String> save(String processId, String preAuthorizeCode, String pin) {
        log.debug("ProcessId: {} AuthServer: Saving pre auth code and pin", processId);
        return pinByPreAuthCodeCacheStore.add(preAuthorizeCode, pin);
    }
}
