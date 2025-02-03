package es.in2.issuer.application.workflow.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.domain.service.impl.CredentialOfferServiceImpl;
import es.in2.issuer.infrastructure.config.AuthServerConfig;
import es.in2.issuer.infrastructure.config.WebClientConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static es.in2.issuer.domain.util.Constants.CONTENT_TYPE;
import static es.in2.issuer.domain.util.Constants.CONTENT_TYPE_APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialOfferIssuanceWorkflowImplTest {

    @Mock
    private AuthServerConfig authServerConfig;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CredentialOfferServiceImpl credentialOfferService;

    @Mock
    private CredentialOfferCacheStorageService credentialOfferCacheStorageService;

    @Mock
    private CredentialProcedureService credentialProcedureService;
    @Mock
    private DeferredCredentialMetadataService deferredCredentialMetadataService;
    @Mock
    private IssuerApiClientTokenService issuerApiClientTokenService;

    @Mock
    private WebClientConfig webClientConfig;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CredentialOfferIssuanceWorkflowImpl credentialOfferIssuanceService;

    @Test
    void testGetCredentialOffer() {
        String id = "dummyId";
        String email = "example@example.com";
        String pin = "1234";
        CustomCredentialOffer credentialOffer = CustomCredentialOffer.builder().build();
        CredentialOfferData credentialOfferData = CredentialOfferData.builder()
                .credentialOffer(credentialOffer)
                .pin(pin)
                .employeeEmail(email)
                .build();

        when(credentialOfferCacheStorageService.getCustomCredentialOffer(id)).thenReturn(Mono.just(credentialOfferData));
        when(emailService.sendPin(credentialOfferData.employeeEmail(),"Pin Code", credentialOfferData.pin())).thenReturn(Mono.empty());

        Mono<CustomCredentialOffer> result = credentialOfferIssuanceService.getCustomCredentialOffer(id);
        assertEquals(credentialOffer, result.block());
    }

    @Test
    void testBuildCredentialOfferUri() throws JsonProcessingException {
        String processId = "1234";
        String transactionCode = "4321";
        String procedureId = "uuid1234";
        String credentialType = "VerifiableCredential";
        String accessToken = "ey1234";
        String nonce = "nonce";
        String credentialOfferUri = "https://example.com/1234";
        String mail = "user@gmail.com";

        PreAuthCodeResponse preAuthCodeResponse = PreAuthCodeResponse.builder()
                .grant(Grant.builder()
                        .preAuthorizedCode("1234")
                        .txCode(Grant.TxCode.builder()
                                .length(4)
                                .build())
                        .build()
                )
                .build();

        CredentialOfferData credentialOfferData = CredentialOfferData.builder()
                .credentialOffer(CustomCredentialOffer.builder().build())
                .pin("1234")
                .employeeEmail(mail)
                .build();


        when(deferredCredentialMetadataService.validateTransactionCode(transactionCode)).thenReturn(Mono.empty());
        when(deferredCredentialMetadataService.getProcedureIdByTransactionCode(transactionCode)).thenReturn(Mono.just(procedureId));
        when(credentialProcedureService.getCredentialTypeByProcedureId(procedureId)).thenReturn(Mono.just(credentialType));

        when(authServerConfig.getPreAuthCodeUri()).thenReturn("https://example.com");
        when(issuerApiClientTokenService.getClientToken()).thenReturn(Mono.just(accessToken));

        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);

        // Create a mock ClientResponse for a successful response
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .header(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .body("PreAuthorizedCode")
                .build();

        // Stub the exchange function to return the mock ClientResponse
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(clientResponse));
        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        when(webClientConfig.commonWebClient()).thenReturn(webClient);

        when(objectMapper.readValue("PreAuthorizedCode", PreAuthCodeResponse.class)).thenReturn(preAuthCodeResponse);
        when(deferredCredentialMetadataService.updateAuthServerNonceByTransactionCode(transactionCode,preAuthCodeResponse.grant().preAuthorizedCode()))
                .thenReturn(Mono.empty());
        when(credentialProcedureService.getMandateeEmailFromDecodedCredentialByProcedureId(procedureId)).thenReturn(Mono.just(mail));
        when(credentialOfferService.buildCustomCredentialOffer(credentialType,preAuthCodeResponse.grant(), mail, preAuthCodeResponse.pin())).thenReturn(Mono.just(credentialOfferData));

        when(credentialOfferCacheStorageService.saveCustomCredentialOffer(credentialOfferData)).thenReturn(Mono.just(nonce));

        when(credentialOfferService.createCredentialOfferUriResponse(nonce)).thenReturn(Mono.just(credentialOfferUri));
        when(deferredCredentialMetadataService.updateCacheStoreForCTransactionCode(transactionCode)).thenReturn(Mono.just("cTransactionCode"));

        StepVerifier.create(credentialOfferIssuanceService.buildCredentialOfferUri(processId,transactionCode))
                .expectNext(CredentialOfferUriResponse.builder()
                        .credentialOfferUri(credentialOfferUri)
                        .cTransactionCode("cTransactionCode")
                        .build())
                .verifyComplete();
    }

    @Test
    void testBuildNewCredentialOfferUri() throws JsonProcessingException {
        String processId = "1234";
        String subTransactionCode = "9876";
        String originalTransactionCode = "4321";
        String procedureId = "uuid1234";
        String credentialType = "VerifiableCredential";
        String accessToken = "ey1234";
        String nonce = "nonce";
        String credentialOfferUri = "https://example.com/1234";
        String mail = "user@gmail.com";

        PreAuthCodeResponse preAuthCodeResponse = PreAuthCodeResponse.builder()
                .grant(Grant.builder()
                        .preAuthorizedCode("1234")
                        .txCode(Grant.TxCode.builder()
                                .length(4)
                                .build())
                        .build()
                )
                .build();

        CredentialOfferData credentialOfferData = CredentialOfferData.builder()
                .credentialOffer(CustomCredentialOffer.builder().build())
                .pin("1234")
                .employeeEmail(mail)
                .build();


        when(deferredCredentialMetadataService.validateCTransactionCode(subTransactionCode)).thenReturn(Mono.just(originalTransactionCode));
        when(deferredCredentialMetadataService.getProcedureIdByTransactionCode(originalTransactionCode)).thenReturn(Mono.just(procedureId));
        when(credentialProcedureService.getCredentialTypeByProcedureId(procedureId)).thenReturn(Mono.just(credentialType));

        when(authServerConfig.getPreAuthCodeUri()).thenReturn("https://example.com");
        when(issuerApiClientTokenService.getClientToken()).thenReturn(Mono.just(accessToken));

        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);

        // Create a mock ClientResponse for a successful response
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .header(CONTENT_TYPE, CONTENT_TYPE_APPLICATION_JSON)
                .body("PreAuthorizedCode")
                .build();

        // Stub the exchange function to return the mock ClientResponse
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(clientResponse));
        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        when(webClientConfig.commonWebClient()).thenReturn(webClient);

        when(objectMapper.readValue("PreAuthorizedCode", PreAuthCodeResponse.class)).thenReturn(preAuthCodeResponse);
        when(deferredCredentialMetadataService.updateAuthServerNonceByTransactionCode(originalTransactionCode,preAuthCodeResponse.grant().preAuthorizedCode()))
                .thenReturn(Mono.empty());
        when(credentialProcedureService.getMandateeEmailFromDecodedCredentialByProcedureId(procedureId)).thenReturn(Mono.just(mail));
        when(credentialOfferService.buildCustomCredentialOffer(credentialType,preAuthCodeResponse.grant(), mail, preAuthCodeResponse.pin())).thenReturn(Mono.just(credentialOfferData));

        when(credentialOfferCacheStorageService.saveCustomCredentialOffer(credentialOfferData)).thenReturn(Mono.just(nonce));

        when(credentialOfferService.createCredentialOfferUriResponse(nonce)).thenReturn(Mono.just(credentialOfferUri));
        when(deferredCredentialMetadataService.updateCacheStoreForCTransactionCode(originalTransactionCode)).thenReturn(Mono.just("cTransactionCode"));

        StepVerifier.create(credentialOfferIssuanceService.buildNewCredentialOfferUri(processId,subTransactionCode))
                .expectNext(CredentialOfferUriResponse.builder()
                        .credentialOfferUri(credentialOfferUri)
                        .cTransactionCode("cTransactionCode")
                        .build())
                .verifyComplete();
    }
}