package es.in2.issuer.application.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.model.Grant;
import es.in2.issuer.domain.service.CredentialIssuerMetadataService;
import es.in2.issuer.domain.service.CredentialOfferCacheStorageService;
import es.in2.issuer.domain.service.VcSchemaService;
import es.in2.issuer.domain.service.impl.CredentialOfferServiceImpl;
import es.in2.issuer.domain.util.HttpUtils;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.iam.service.GenericIamAdapter;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import es.in2.issuer.infrastructure.repository.CacheStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CredentialOfferIssuanceServiceImplTest {
    @Mock
    private CacheStore<CustomCredentialOffer> cacheStore;
    @Mock
    private AppConfiguration appConfiguration;

    @Mock
    private IamAdapterFactory iamAdapterFactory;

    @Mock
    private GenericIamAdapter genericIamAdapter;

    @Mock
    private HttpUtils httpUtils;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CredentialIssuerMetadataService credentialIssuerMetadataService;
    @Mock
    private CredentialOfferServiceImpl credentialOfferService;
    @Mock
    private CredentialOfferCacheStorageService credentialOfferCacheStorageService;
    @Mock
    private VcSchemaService vcSchemaService;
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
        when(httpUtils.prepareHeadersWithAuth(token)).thenReturn(Mono.just(headers));
        when(httpUtils.getRequest(getPreAuthCodeUri + "?type=VerifiableId&format=jwt_vc_json", headers)).thenReturn(Mono.just(jsonString));

        // Mock objectMapper to return a non-null Grant
        Grant mockGrant = Grant.builder().build(); // Assume Grant is a simple class, adjust accordingly
        when(objectMapper.readValue(jsonString, Grant.class)).thenReturn(mockGrant);

        when(credentialOfferService.buildCustomCredentialOffer(credentialType, mockGrant)).thenReturn(Mono.just(credentialOffer));
        when(credentialOfferCacheStorageService.saveCustomCredentialOffer(credentialOffer)).thenReturn(Mono.just(nonce));
        when(credentialOfferService.createCredentialOfferUri(nonce)).thenReturn(Mono.just(credentialOfferUri));

        Mono<String> result = credentialOfferIssuanceService.buildCredentialOfferUri(token, credentialType);
        assertEquals(credentialOfferUri, result.block());
    }

}