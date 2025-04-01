package es.in2.issuer.authserver.domain.service.impl;

import es.in2.issuer.authserver.domain.service.PreAuthorizedCodeCacheStore;
import es.in2.issuer.shared.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PreAuthorizedCodeCacheStoreImpl implements PreAuthorizedCodeCacheStore {
    private final CacheStore<String> txCodeByPreAuthorizedCodeCacheStore;

    @Override
    public Mono<String> save(String processId, String preAuthorizeCode, String txCode) {
        log.debug("ProcessId: {} AuthServer: Saving PreAuthorizedCode and TxCode", processId);
        return txCodeByPreAuthorizedCodeCacheStore.add(preAuthorizeCode, txCode);
    }
}
