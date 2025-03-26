package es.in2.issuer.authserver.domain.service;

import es.in2.issuer.authserver.domain.service.impl.PreAuthCodeCacheStoreImpl;
import es.in2.issuer.shared.infrastructure.repository.CacheStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreAuthCodeCacheStoreImplTest {

    @Mock
    CacheStore<String> cacheStore;

    @InjectMocks
    PreAuthCodeCacheStoreImpl preAuthCodeCacheStore;

    @Test
    void itShouldSave() {
        String expectedPreAuthCode = "1234";
        String pin = "5678";

        when(cacheStore.add(expectedPreAuthCode, pin)).thenReturn(Mono.just(expectedPreAuthCode));

        var resultMono = preAuthCodeCacheStore.save("", expectedPreAuthCode, pin);

        StepVerifier
                .create(resultMono)
                .assertNext(result -> assertThat(result).isEqualTo(expectedPreAuthCode))
                .verifyComplete();
    }
}