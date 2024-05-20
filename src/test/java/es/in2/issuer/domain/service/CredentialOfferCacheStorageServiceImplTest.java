package es.in2.issuer.domain.service;


import es.in2.issuer.domain.exception.CustomCredentialOfferNotFoundException;
import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.service.impl.CredentialOfferCacheStorageServiceImpl;
import es.in2.issuer.infrastructure.repository.CacheStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CredentialOfferCacheStorageServiceImplTest {

    @Mock
    private CacheStore<CustomCredentialOffer> cacheStore;

    @InjectMocks
    private CredentialOfferCacheStorageServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveCustomCredentialOffer() {
        CustomCredentialOffer offer = CustomCredentialOffer.builder().build(); // You should populate it as necessary
        String expectedNonce = "testNonce";

        when(cacheStore.add(any(String.class), eq(offer))).thenReturn(Mono.just(expectedNonce));

        StepVerifier.create(service.saveCustomCredentialOffer(offer))
                .expectNext(expectedNonce)
                .verifyComplete();

        verify(cacheStore, times(1)).add(any(String.class), eq(offer));
    }

    @Test
    void testGetCustomCredentialOffer() {
        String nonce = "testNonce";
        CustomCredentialOffer offer = CustomCredentialOffer.builder().build(); // Populate this object as necessary

        when(cacheStore.get(nonce)).thenReturn(Mono.just(offer));
        doNothing().when(cacheStore).delete(nonce);

        StepVerifier.create(service.getCustomCredentialOffer(nonce))
                .expectNextMatches(retrievedOffer -> retrievedOffer.equals(offer))
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