package es.in2.issuer.oidc4vci.domain.service.impl;

import es.in2.issuer.shared.infrastructure.repository.CacheStoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NonceValidationServiceImplTest {

    @Mock
    private CacheStoreRepository<String> nonceCacheStore;

    @InjectMocks
    private NonceValidationServiceImpl nonceValidationService;

    @Test
    void itShouldReturnTrueWhenValidNonce() {
        String nonce = "test-nonce";
        when(nonceCacheStore.get(nonce))
                .thenReturn(Mono.just(nonce));

        var resultMono = nonceValidationService.validate(nonce);

        StepVerifier
                .create(resultMono)
                .assertNext(result ->
                        assertThat(result.isNonceValid()).isTrue())
                .verifyComplete();
    }

    @Test
    void itShouldFalseWhenInvalidNonce() {
        String nonce = "test-nonce";
        when(nonceCacheStore.get(nonce))
                .thenReturn(Mono.error(new NoSuchElementException()));

        var resultMono = nonceValidationService.validate(nonce);

        StepVerifier
                .create(resultMono)
                .assertNext(result ->
                        assertThat(result.isNonceValid()).isFalse())
                .verifyComplete();
    }

    @Test
    void itShouldReturnExceptionWhenInvalidNonce() {
        String nonce = "test-nonce";
        when(nonceCacheStore.get(nonce))
                .thenReturn(Mono.error(new RuntimeException()));

        var resultMono = nonceValidationService.validate(nonce);

        StepVerifier
                .create(resultMono)
                .expectError(Exception.class)
                .verify();
    }
}