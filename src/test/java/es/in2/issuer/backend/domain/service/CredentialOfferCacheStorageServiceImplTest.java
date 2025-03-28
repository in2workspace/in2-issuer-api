package es.in2.issuer.backend.domain.service;

import es.in2.issuer.backend.domain.exception.CustomCredentialOfferNotFoundException;
import es.in2.issuer.backend.domain.model.dto.CredentialOfferData;
import es.in2.issuer.backend.domain.service.impl.CredentialOfferCacheStorageServiceImpl;
import es.in2.issuer.shared.infrastructure.repository.CacheStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialOfferCacheStorageServiceImplTest {

    @Mock
    private CacheStore<CredentialOfferData> cacheStore;

    @InjectMocks
    private CredentialOfferCacheStorageServiceImpl service;

    @Test
    void testSaveCustomCredentialOffer() {
        CredentialOfferData credentialOfferData = CredentialOfferData.builder().build(); // You should populate it as necessary
        String expectedNonce = "testNonce";

        when(cacheStore.add(any(String.class), eq(credentialOfferData))).thenReturn(Mono.just(expectedNonce));

        StepVerifier.create(service.saveCustomCredentialOffer(credentialOfferData))
                .expectNext(expectedNonce)
                .verifyComplete();

        verify(cacheStore, times(1)).add(any(String.class), eq(credentialOfferData));
    }

    @Test
    void testGetCustomCredentialOffer() {
        String nonce = "testNonce";
        CredentialOfferData credentialOfferData = CredentialOfferData.builder().build(); // You should populate it as necessary

        when(cacheStore.get(nonce)).thenReturn(Mono.just(credentialOfferData));
        when(cacheStore.delete(nonce)).thenReturn(Mono.empty());

        StepVerifier.create(service.getCustomCredentialOffer(nonce))
                .expectNextMatches(retrievedOffer -> retrievedOffer.equals(credentialOfferData))
                .verifyComplete();

        verify(cacheStore, times(1)).delete(nonce);
    }

    @Test
    void testGetCustomCredentialOfferNotFound() {
        String nonce = "testNonce";
        when(cacheStore.get(nonce)).thenReturn(Mono.empty());

        StepVerifier.create(service.getCustomCredentialOffer(nonce))
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable)
                            .isInstanceOf(CustomCredentialOfferNotFoundException.class)
                            .hasMessageContaining("CustomCredentialOffer not found for nonce: " + nonce);
                })
                .verify();

        verify(cacheStore, never()).delete(anyString());
    }

}