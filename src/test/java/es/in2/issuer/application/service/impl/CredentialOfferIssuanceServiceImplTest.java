package es.in2.issuer.application.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.model.Grant;
import es.in2.issuer.domain.model.PreAuthCodeResponse;
import es.in2.issuer.domain.service.CredentialOfferCacheStorageService;
import es.in2.issuer.domain.service.EmailService;
import es.in2.issuer.domain.service.VcSchemaService;
import es.in2.issuer.domain.service.impl.CredentialOfferServiceImpl;
import es.in2.issuer.infrastructure.config.WebClientConfig;
import es.in2.issuer.infrastructure.iam.service.GenericIamAdapter;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialOfferIssuanceServiceImplTest {
    @Mock
    private IamAdapterFactory iamAdapterFactory;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CredentialOfferServiceImpl credentialOfferService;
    @Mock
    private CredentialOfferCacheStorageService credentialOfferCacheStorageService;
    @Mock
    private VcSchemaService vcSchemaService;
    @Mock
    private WebClientConfig webClientConfig;
    @Mock
    private EmailService emailService;
    @InjectMocks
    private CredentialOfferIssuanceServiceImpl credentialOfferIssuanceService;

    @Test
    void testGetCredentialOffer() {
        String id = "dummyId";
        CustomCredentialOffer credentialOffer = CustomCredentialOffer.builder().build();
        when(credentialOfferCacheStorageService.getCustomCredentialOffer(id)).thenReturn(Mono.just(credentialOffer));

        Mono<CustomCredentialOffer> result = credentialOfferIssuanceService.getCustomCredentialOffer(id);
        assertEquals(credentialOffer, result.block());
    }

    @Test
    void testBuildCredentialOfferUri() throws JsonProcessingException {
        String token = "dummyToken";
        String credentialType = "dummyType";
        String nonce = "dummyNonce";
        String getPreAuthCodeUri = "https://iam.example.com/PreAuthCodeUri";
        String credentialOfferUri = "dummyCredentialOfferUri";
        CustomCredentialOffer credentialOffer = CustomCredentialOffer.builder().build();
        when(vcSchemaService.isSupportedVcSchema(credentialType)).thenReturn(Mono.just(true));

        List<Map.Entry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE));

        GenericIamAdapter adapter = mock(GenericIamAdapter.class);
        when(iamAdapterFactory.getAdapter()).thenReturn(adapter);
        when(adapter.getPreAuthCodeUri()).thenReturn(getPreAuthCodeUri);

        // Ensure the JSON is valid and corresponds to a Grant object
        String jsonString = "{\"pre-authorized_code\":\"your_pre_authorized_code_here\"}";
        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);

        // Create a mock ClientResponse for a successful response
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(jsonString)
                .build();

        // Stub the exchange function to return the mock ClientResponse
        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(clientResponse));

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        when(webClientConfig.centralizedWebClient()).thenReturn(webClient);

        // Mock objectMapper to return a non-null Grant
        Grant mockGrant = Grant.builder().build(); // Assume Grant is a simple class, adjust accordingly
        PreAuthCodeResponse preAuthCodeResponse = PreAuthCodeResponse.builder().grant(mockGrant).pin("1234").build();
        when(objectMapper.readValue(jsonString, PreAuthCodeResponse.class)).thenReturn(preAuthCodeResponse);

        when(emailService.sendPin(any(), any(), any())).thenReturn(Mono.empty());

        when(credentialOfferService.buildCustomCredentialOffer(credentialType, mockGrant)).thenReturn(Mono.just(credentialOffer));
        when(credentialOfferCacheStorageService.saveCustomCredentialOffer(credentialOffer)).thenReturn(Mono.just(nonce));
        when(credentialOfferService.createCredentialOfferUri(nonce)).thenReturn(Mono.just(credentialOfferUri));

        Mono<String> result = credentialOfferIssuanceService.buildCredentialOfferUri(token, credentialType);
        assertEquals(credentialOfferUri, result.block());
    }

}