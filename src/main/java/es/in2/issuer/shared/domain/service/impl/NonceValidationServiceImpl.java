package es.in2.issuer.shared.domain.service.impl;

import es.in2.issuer.shared.domain.service.NonceValidationService;
import es.in2.issuer.shared.infrastructure.repository.CacheStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class NonceValidationServiceImpl implements NonceValidationService {
    private final CacheStoreRepository<String> nonceCacheStore;

    @Override
    public Mono<Boolean> isValid(String processId, Mono<String> nonceMono) {
        return ensureNonceIsValid(nonceMono)
                .doFirst(() -> log.debug("ProcessId: {} AuthServer: Validating nonce", processId))
                .doOnSuccess(result ->
                        log.debug("ProcessId: {} AuthServer: Nonce validated. Result. Result: isValid = {}",
                                processId, result));
    }

    private Mono<Boolean> ensureNonceIsValid(Mono<String> nonceMono) {
        return nonceMono.flatMap(nonce ->
                nonceCacheStore
                        .get(nonce)
                        .map(storedNonce -> storedNonce.equals(nonce))
                        .onErrorResume(ex ->
                                ex instanceof NoSuchElementException
                                        ? Mono.just(Boolean.FALSE)
                                        : Mono.error(ex)));
    }
}