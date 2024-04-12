package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.AppNonceValidationResponse;
import es.in2.issuer.domain.model.NonceResponse;
import es.in2.issuer.domain.service.impl.NonceManagementServiceImpl;
import es.in2.issuer.infrastructure.repository.CacheStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NonceManagementServiceImplTest {

    @Mock
    private CacheStore<String> cacheStore;

    @InjectMocks
    private NonceManagementServiceImpl nonceManagementService;

    @Test
    void testSaveAccessTokenAndNonce() {
        String accessToken = "testToken";
        AppNonceValidationResponse response = mock(AppNonceValidationResponse.class);
        when(response.accessToken()).thenReturn(accessToken);

        // Simulate the cache behavior
        when(cacheStore.add(anyString(), eq(accessToken))).thenReturn(Mono.empty());

        Mono<NonceResponse> result = nonceManagementService.saveAccessTokenAndNonce(response);

        StepVerifier.create(result)
                .assertNext(nonceResponse -> {
                    assertNotNull(nonceResponse.nonce());
                    assertEquals("600", nonceResponse.nonceExpiresIn());
                })
                .verifyComplete();

        verify(cacheStore, times(1)).add(anyString(), eq(accessToken));
    }
}