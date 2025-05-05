package es.in2.issuer.backend.oidc4vci.infrastructure.controller;

import es.in2.issuer.backend.oidc4vci.application.workflow.CredentialOfferWorkflow;
import es.in2.issuer.backend.shared.domain.model.dto.CredentialOffer;
import es.in2.issuer.backend.shared.domain.model.dto.Grants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialOfferControllerTest {

    @Mock
    private CredentialOfferWorkflow credentialOfferWorkflow;

    @InjectMocks
    private CredentialOfferController credentialOfferController;

    @Test
    void getCredentialOffer() {
        // Arrange
        String credentialOfferId = "e0e2c6ab-8fe7-4709-82f0-4a771aaee841";
        Grants grants = Grants.builder()
                .preAuthorizedCode("oaKazRN8I0IbtZ0C7JuMn5")
                .txCode(Grants.TxCode.builder()
                        .length(4)
                        .inputMode("numeric")
                        .description("Please provide the one-time code that was sent via e-mail")
                        .build())
                .build();
        CredentialOffer credentialOffer = CredentialOffer.builder()
                .credentialIssuer("https://credential-issuer.example.com")
                .credentialConfigurationIds(List.of("LEARCredentialEmployee"))
                .grants(Map.of("urn:ietf:params:oauth:grant-type:pre-authorized_code", grants))
                .build();
        // Mock
        when(credentialOfferWorkflow.getCredentialOfferById(anyString(), anyString()))
                .thenReturn(Mono.just(credentialOffer));
        // Act
        Mono<CredentialOffer> result = credentialOfferController.getCredentialOfferByReference(
                credentialOfferId,
                MockServerWebExchange.from(MockServerHttpRequest.get("/"))
        );
        // Assert
        StepVerifier.create(result)
                .assertNext(credentialOfferFound -> assertEquals(credentialOffer, credentialOfferFound))
                .verifyComplete();
    }

}
