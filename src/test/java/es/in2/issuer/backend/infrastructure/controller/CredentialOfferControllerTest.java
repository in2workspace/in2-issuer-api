package es.in2.issuer.backend.infrastructure.controller;

import es.in2.issuer.backend.application.workflow.CredentialOfferIssuanceWorkflow;
import es.in2.issuer.backend.domain.model.dto.CredentialOfferUriResponse;
import es.in2.issuer.shared.domain.model.dto.CustomCredentialOffer;
import es.in2.issuer.shared.domain.model.dto.Grant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialOfferControllerTest {

    @Mock
    private CredentialOfferIssuanceWorkflow credentialOfferIssuanceWorkflow;

    @InjectMocks
    private CredentialOfferController credentialOfferController;

    @Test
    void getCredentialOfferByTransactionCode() {
        //Arrange
        String transactionCode = "testTransactionCode";
        String credentialOfferUri = "https://www.goodair.com/credential-offer?credential_offer_uri=https://www.goodair.com/credential-offer/5j349k3e3n23j";
        String cTransactionCode = "testCTransactionCode";
        CredentialOfferUriResponse credentialOfferUriResponse = CredentialOfferUriResponse.builder()
                .cTransactionCode(cTransactionCode)
                .credentialOfferUri(credentialOfferUri)
                .build();
        when(credentialOfferIssuanceWorkflow.buildCredentialOfferUri(anyString(), eq(transactionCode))).thenReturn(Mono.just(credentialOfferUriResponse));

        //Act
        Mono<CredentialOfferUriResponse> result = credentialOfferController.getCredentialOfferByTransactionCode(transactionCode);

        //Assert
        StepVerifier.create(result)
                .assertNext(credentialOfferUriResponse1 -> assertEquals(credentialOfferUriResponse, credentialOfferUriResponse1))
                .verifyComplete();
    }

    @Test
    void getNewCredentialOfferByTransactionCode() {
        //Arrange
        String transactionCode = "testTransactionCode";
        String credentialOfferUri = "https://www.goodair.com/credential-offer?credential_offer_uri=https://www.goodair.com/credential-offer/5j349k3e3n23j";
        String cTransactionCode = "testCTransactionCode";
        CredentialOfferUriResponse credentialOfferUriResponse = CredentialOfferUriResponse.builder()
                .cTransactionCode(cTransactionCode)
                .credentialOfferUri(credentialOfferUri)
                .build();
        when(credentialOfferIssuanceWorkflow.buildNewCredentialOfferUri(anyString(), eq(transactionCode))).thenReturn(Mono.just(credentialOfferUriResponse));

        //Act
        Mono<CredentialOfferUriResponse> result = credentialOfferController.getCredentialOfferByCTransactionCode(transactionCode);

        //Assert
        StepVerifier.create(result)
                .assertNext(credentialOfferUriResponse1 -> assertEquals(credentialOfferUriResponse, credentialOfferUriResponse1))
                .verifyComplete();
    }

    @Test
    void getCredentialOffer() {
        //Arrange
        Grant.TxCode txCode = new Grant.TxCode(5, "input_mode", "description");
        Grant.TxCode txCode2 = new Grant.TxCode(5, "input_mode", "description");
        Map<String, Grant> grants = new HashMap<>();
        grants.put("grant1", new Grant("pre-authorized_code", txCode));
        grants.put("grant2", new Grant("pre-authorized_code", txCode2));

        CustomCredentialOffer customCredentialOffer = CustomCredentialOffer.builder()
                .credentialIssuer("https://client-issuer.com")
                .credentialConfigurationIds(List.of("UniversityDegree"))
                .credentials(List.of(new CustomCredentialOffer.Credential("format", List.of("type"))))
                .grants(grants)
                .build();

        String id = "testId";

        when(credentialOfferIssuanceWorkflow.getCustomCredentialOffer(id)).thenReturn(Mono.just(customCredentialOffer));

        //Act
        Mono<CustomCredentialOffer> result = credentialOfferController.getCredentialOffer(id, MockServerWebExchange.from(MockServerHttpRequest.get("/")));

        //Assert
        StepVerifier.create(result)
                .assertNext(offer -> assertEquals(customCredentialOffer, offer))
                .verifyComplete();
    }
}