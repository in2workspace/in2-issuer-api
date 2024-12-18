package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.CredentialOfferIssuanceWorkflow;
import es.in2.issuer.domain.model.dto.CustomCredentialOffer;
import es.in2.issuer.domain.model.dto.Grant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
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
        when(credentialOfferIssuanceWorkflow.buildCredentialOfferUri(anyString(), eq(transactionCode))).thenReturn(Mono.just(credentialOfferUri));

        //Act
        Mono<String> result = credentialOfferController.getCredentialOfferByTransactionCode(transactionCode);

        //Assert
        StepVerifier.create(result)
                .assertNext(uri -> assertEquals(credentialOfferUri, uri))
                .verifyComplete();
    }

//    @Test
//    void getCredentialOffer() {
//        //Arrange
//        Grant.TxCode txCode = new Grant.TxCode(5, "input_mode", "description");
//        Grant.TxCode txCode2 = new Grant.TxCode(5, "input_mode", "description");
//        Map<String, Grant> grants = new HashMap<>();
//        grants.put("grant1", new Grant("pre-authorized_code", txCode));
//        grants.put("grant2", new Grant("pre-authorized_code", txCode2));
//
//        CustomCredentialOffer customCredentialOffer = CustomCredentialOffer.builder()
//                .credentialIssuer("https://client-issuer.com")
//                .credentialConfigurationIds(List.of("UniversityDegree"))
//                .credentials(List.of(new CustomCredentialOffer.Credential("format", List.of("type"))))
//                .grants(grants)
//                .build();
//
//        String id = "testId";
//        String email = "test@example.com";
//        Grant.PreAuthorizationCodeResponse preAuthCodeResponse = new Grant.PreAuthorizationCodeResponse(
//                new Grant.PreAuthorizedCode("preAuthCode123", null)
//        );
//
//        when(credentialOfferIssuanceWorkflow.getCustomCredentialOffer(id))
//                .thenReturn(Mono.just(customCredentialOffer));
//        when(credentialOfferIssuanceWorkflow.getPreAuthorizationCodeFromIam())
//                .thenReturn(Mono.just(preAuthCodeResponse));
//        when(credentialProcedureService.getMandateeEmailFromDecodedCredentialByProcedureId(id))
//                .thenReturn(Mono.just(email));
//        when(emailService.sendPin(eq(email), eq("Pin Code"), eq(preAuthCodeResponse.grant().preAuthorizedCode())))
//                .thenReturn(Mono.empty());
//
//        MockServerHttpRequest request = MockServerHttpRequest.get("/").build();
//        ServerWebExchange exchange = MockServerWebExchange.from(request);
//
//        //Act
//        Mono<CustomCredentialOffer> result = credentialOfferController.getCredentialOffer(id, exchange);
//
//        //Assert
//        StepVerifier.create(result)
//                .assertNext(offer -> assertEquals(customCredentialOffer, offer))
//                .verifyComplete();
//
//        //Verify interactions
//        verify(credentialOfferIssuanceWorkflow).getCustomCredentialOffer(id);
//        verify(credentialOfferIssuanceWorkflow).getPreAuthorizationCodeFromIam();
//        verify(credentialProcedureService).getMandateeEmailFromDecodedCredentialByProcedureId(id);
//        verify(emailService).sendPin(eq(email), eq("Pin Code"), eq(preAuthCodeResponse.grant().preAuthorizedCode()));
//    }

}