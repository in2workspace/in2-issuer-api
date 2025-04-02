package es.in2.issuer.authserver.domain.service.impl;

import es.in2.issuer.authserver.domain.service.PreAuthorizedCodeCacheStore;
import es.in2.issuer.shared.domain.model.dto.CredentialIdAndTxCode;
import es.in2.issuer.shared.infrastructure.repository.CacheStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PreAuthorizedCodeCacheStoreImpl implements PreAuthorizedCodeCacheStore {
    private final CacheStoreRepository<CredentialIdAndTxCode> credentialIdAndTxCodeByPreAuthorizedCodeCacheStoreRepository;

    @Override
    public Mono<String> save(String processId, String preAuthorizeCode, UUID credentialId, String txCode) {
        log.debug("ProcessId: {} AuthServer: Saving PreAuthorizedCode and TxCode", processId);
        return credentialIdAndTxCodeByPreAuthorizedCodeCacheStoreRepository.add(
                preAuthorizeCode,
                new CredentialIdAndTxCode(credentialId, txCode));
    }
}
