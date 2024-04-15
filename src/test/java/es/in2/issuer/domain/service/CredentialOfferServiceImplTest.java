package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.service.impl.CredentialOfferServiceImpl;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static es.in2.issuer.domain.util.Constants.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class CredentialOfferServiceImplTest {

    @Mock
    private AppConfiguration appConfiguration;

    @InjectMocks
    private CredentialOfferServiceImpl credentialOfferService;

    @BeforeEach
    void setUp() {
        when(appConfiguration.getIssuerDomain()).thenReturn("https://example.com");
    }

    @Test
    void testBuildCustomCredentialOffer() {
        String credentialType = "type1";
        String preAuthCode = "code123";

        Mono<CustomCredentialOffer> result = credentialOfferService.buildCustomCredentialOffer(credentialType, preAuthCode);

        StepVerifier.create(result)
                .expectNextMatches(offer ->
                        offer.credentialIssuer().equals("https://example.com") &&
                                offer.credentials().size() == 2 &&
                                offer.credentialConfigurationIds().equals(List.of(LEAR_CREDENTIAL_JWT, LEAR_CREDENTIAL_CWT)) &&
                                offer.grants().containsKey(GRANT_TYPE)
                )
                .verifyComplete();
    }

    @Test
    void testCreateCredentialOfferUri() {
        String nonce = "abc123";

        Mono<String> result = credentialOfferService.createCredentialOfferUri(nonce);

        StepVerifier.create(result)
                .expectNext("https://example.com/credential-offer?credential_offer_uri=https://example.com/credential-offer/abc123")
                .verifyComplete();
    }

    // Additional helper method used in the service
    private String ensureUrlHasProtocol(String url) {
        return url.startsWith("http://") || url.startsWith("https://") ? url : "https://" + url;
    }
}