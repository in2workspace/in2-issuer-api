package es.in2.issuer.oidc4vci.domain.service.impl;

import es.in2.issuer.oidc4vci.domain.service.NonceValidationService;
import es.in2.issuer.shared.domain.model.dto.NonceValidationResponse;
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
    public Mono<NonceValidationResponse> validate(String nonce) {
        return Mono.defer(() -> ensureNonceIsValid(nonce));
    }

    private Mono<NonceValidationResponse> ensureNonceIsValid(String nonce) {
        return nonceCacheStore
                .get(nonce)
                .map(storedNonce -> new NonceValidationResponse(storedNonce.equals(nonce)))
                .onErrorResume(ex ->
                        ex instanceof NoSuchElementException
                                ? Mono.just(new NonceValidationResponse(false))
                                : Mono.error(ex));
    }
}
