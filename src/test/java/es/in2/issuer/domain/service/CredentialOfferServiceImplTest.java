//package es.in2.issuer.domain.service;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import es.in2.issuer.domain.exception.ExpiredPreAuthorizedCodeException;
//import es.in2.issuer.domain.model.CredentialIssuerMetadata;
//import es.in2.issuer.domain.model.CustomCredentialOffer;
//import es.in2.issuer.domain.model.CredentialsSupported;
//import es.in2.issuer.domain.model.VcTemplate;
//import es.in2.issuer.domain.service.impl.CredentialOfferServiceImpl;
//import es.in2.issuer.domain.util.HttpUtils;
//import es.in2.issuer.infrastructure.config.AppConfiguration;
//import es.in2.issuer.infrastructure.iam.service.GenericIamAdapter;
//import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
//import es.in2.issuer.infrastructure.repository.CacheStore;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.test.util.ReflectionTestUtils;
//import reactor.core.Exceptions;
//import reactor.core.publisher.Mono;
//import com.fasterxml.jackson.databind.JsonNode;
//
//import java.util.*;
//
//import static es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.*;
//@ExtendWith(MockitoExtension.class)
//class CredentialOfferServiceImplTest {
//
//    @Mock
//    private CacheStore<CustomCredentialOffer> cacheStore;
//    @Mock
//    private AppConfiguration appConfiguration;
//
//    @Mock
//    private IamAdapterFactory iamAdapterFactory;
//
//    @Mock
//    private GenericIamAdapter genericIamAdapter;
//
//    @Mock
//    private HttpUtils httpUtils;
//
//    @Mock
//    private ObjectMapper objectMapper;
//
//    @Mock
//    private CredentialIssuerMetadataService credentialIssuerMetadataService;
//
//    @InjectMocks
//    private CredentialOfferServiceImpl credentialOfferService;
//
//    @Test
//    void testCreateCredentialOfferUriForPreAuthorizedCodeFlow() throws JsonProcessingException, JsonProcessingException {
//        // Mock the getPreAuthCodeUri() response
//        String mockPreAuthCodeUri = "http://mocktokenuri";
//        when(iamAdapterFactory.getAdapter()).thenReturn(genericIamAdapter);
//        when(genericIamAdapter.getPreAuthCodeUri()).thenReturn(mockPreAuthCodeUri);
//
//        ReflectionTestUtils.setField(credentialOfferService,"issuerApiBaseUrl","http://baseUrl");
//        String url = "http://mocktokenuri?type=VerifiableId&format=jwt_vc_json";
//
//        String accessToken = "dummyAccessToken";
//        List<CredentialsSupported> supportedParameters = Arrays.asList(
//                new CredentialsSupported(
//                        "jwt_vc_json",
//                        "VerifiableId_JWT",
//                        Arrays.asList("VerifiableCredential", "VerifiableAttestation", "VerifiableId"),
//                        List.of("did"),
//                        List.of(),
//                        VcTemplate.builder().mutable(false).name("VerifiableId_JWT").template(null).build()
//                ),
//                new CredentialsSupported(
//                        "jwt_vc_json",
//                        LEAR_CREDENTIAL,
//                        Arrays.asList("VerifiableCredential", "VerifiableAttestation", "LEARCredential"),
//                        List.of("did"),
//                        List.of(),
//                        VcTemplate.builder().mutable(false).name(LEAR_CREDENTIAL).template(null).build()
//                )
//        );
//
//        CredentialIssuerMetadata credentialIssuerMetadata = new CredentialIssuerMetadata("credentialIssuer","credentialEndpoint","credentialToken", supportedParameters );
//
//        when(credentialIssuerMetadataService.generateOpenIdCredentialIssuer()).thenReturn(Mono.just(credentialIssuerMetadata));
//
//        List<Map.Entry<String, String>> headers = new ArrayList<>();
//        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
//        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE));
//
//        String jsonString = "{\"grants\":{\"pre-authorized_code\":\"your_pre_authorized_code_here\"}}";
//        when(httpUtils.getRequest(url,headers)).thenReturn(Mono.just(jsonString));
//        JsonNode jsonObject = new ObjectMapper().convertValue(jsonString, JsonNode.class);
//        when(objectMapper.readTree(jsonString)).thenReturn(jsonObject);
//
//        String result = credentialOfferService.createCredentialOfferUriForPreAuthorizedCodeFlow(accessToken, LEAR_CREDENTIAL).block();
//
//        Assertions.assertNotNull(result);
//
//    }
//
//    @Test
//    void testGenerateCredentialOffer() throws JsonProcessingException {
//        // Mock the getPreAuthCodeUri() response
//        String mockPreAuthCodeUri = "http://mocktokenuri";
//        when(iamAdapterFactory.getAdapter()).thenReturn(genericIamAdapter);
//        when(genericIamAdapter.getPreAuthCodeUri()).thenReturn(mockPreAuthCodeUri);
//
//        ReflectionTestUtils.setField(credentialOfferService,"issuerApiBaseUrl","http://baseUrl");
//        String url = "http://mocktokenuri?type=VerifiableId&format=jwt_vc_json";
//
//        String accessToken = "dummyAccessToken";
//        List<CredentialsSupported> supportedParameters = Arrays.asList(
//                new CredentialsSupported(
//                        "jwt_vc_json",
//                        "VerifiableId_JWT",
//                        Arrays.asList("VerifiableCredential", "VerifiableAttestation", "VerifiableId"),
//                        List.of("did"),
//                        List.of(),
//                        VcTemplate.builder().mutable(false).name("VerifiableId_JWT").template(null).build()
//                ),
//                new CredentialsSupported(
//                        "jwt_vc_json",
//                        LEAR_CREDENTIAL,
//                        Arrays.asList("VerifiableCredential", "VerifiableAttestation", "LEARCredential"),
//                        List.of("did"),
//                        List.of(),
//                        VcTemplate.builder().mutable(false).name(LEAR_CREDENTIAL).template(null).build()
//                )
//        );
//
//        CredentialIssuerMetadata credentialIssuerMetadata = new CredentialIssuerMetadata("credentialIssuer","credentialEndpoint","credentialToken", supportedParameters );
//
//        when(credentialIssuerMetadataService.generateOpenIdCredentialIssuer()).thenReturn(Mono.just(credentialIssuerMetadata));
//
//        List<Map.Entry<String, String>> headers = new ArrayList<>();
//        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
//        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE));
//
//        String jsonString = "{\"grants\":{\"pre-authorized_code\":\"your_pre_authorized_code_here\"}}";
//        when(httpUtils.getRequest(url,headers)).thenReturn(Mono.just(jsonString));
//        JsonNode jsonObject = new ObjectMapper().convertValue(jsonString, JsonNode.class);
//        when(objectMapper.readTree(jsonString)).thenReturn(jsonObject);
//
//        // Call method
//        Mono<String> result = credentialOfferService.generateCredentialOffer(accessToken, LEAR_CREDENTIAL);
//
//        String dummyNonce = "XvmPFalLQWKdfnXcDUvAOA";
//        // Assertions
//        assertNotNull(result);
//        assertEquals(dummyNonce.length(), result.block().length());
//        verify(credentialIssuerMetadataService, times(1)).generateOpenIdCredentialIssuer();
//        verify(httpUtils, times(1)).getRequest(url,headers);
//        verify(objectMapper, times(1)).readTree(jsonString);
//
//    }
//
//    @Test
//    void testGenerateCredentialOfferWithNullCredentialType() throws JsonProcessingException {
//        // Mock the getPreAuthCodeUri() response
//        String mockPreAuthCodeUri = "http://mocktokenuri";
//        when(iamAdapterFactory.getAdapter()).thenReturn(genericIamAdapter);
//        when(genericIamAdapter.getPreAuthCodeUri()).thenReturn(mockPreAuthCodeUri);
//
//        ReflectionTestUtils.setField(credentialOfferService,"issuerApiBaseUrl","http://baseUrl");
//        String url = "http://mocktokenuri?type=VerifiableId&format=jwt_vc_json";
//
//        String accessToken = "dummyAccessToken";
//        List<CredentialsSupported> supportedParameters = Arrays.asList(
//                new CredentialsSupported(
//                        "jwt_vc_json",
//                        "VerifiableId_JWT",
//                        Arrays.asList("VerifiableCredential", "VerifiableAttestation", "VerifiableId"),
//                        List.of("did"),
//                        List.of(),
//                        VcTemplate.builder().mutable(false).name("VerifiableId_JWT").template(null).build()
//                ),
//                new CredentialsSupported(
//                        "jwt_vc_json",
//                        LEAR_CREDENTIAL,
//                        Arrays.asList("VerifiableCredential", "VerifiableAttestation", "LEARCredential"),
//                        List.of("did"),
//                        List.of(),
//                        VcTemplate.builder().mutable(false).name(LEAR_CREDENTIAL).template(null).build()
//                )
//        );
//
//        CredentialIssuerMetadata credentialIssuerMetadata = new CredentialIssuerMetadata("credentialIssuer","credentialEndpoint","credentialToken", supportedParameters );
//
//        when(credentialIssuerMetadataService.generateOpenIdCredentialIssuer()).thenReturn(Mono.just(credentialIssuerMetadata));
//
//        List<Map.Entry<String, String>> headers = new ArrayList<>();
//        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken));
//        headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE));
//
//        String jsonString = "{\"grants\":{\"pre-authorized_code\":\"your_pre_authorized_code_here\"}}";
//        when(httpUtils.getRequest(url,headers)).thenReturn(Mono.just(jsonString));
//        JsonNode jsonObject = new ObjectMapper().convertValue(jsonString, JsonNode.class);
//        when(objectMapper.readTree(jsonString)).thenReturn(jsonObject);
//
//        // Call method
//        Mono<String> result = credentialOfferService.generateCredentialOffer(accessToken, null);
//
//        String dummyNonce = "XvmPFalLQWKdfnXcDUvAOA";
//        // Assertions
//        assertNotNull(result);
//        assertEquals(dummyNonce.length(), result.block().length());
//        verify(credentialIssuerMetadataService, times(1)).generateOpenIdCredentialIssuer();
//        verify(httpUtils, times(1)).getRequest(url,headers);
//        verify(objectMapper, times(1)).readTree(jsonString);
//
//    }
//
//    @Test
//    void testGetCredentialOffer() {
//        String id = "dummyId";
//        CustomCredentialOffer credentialOffer = CustomCredentialOffer.builder().build();
//
//        when(cacheStore.get(id)).thenReturn(credentialOffer);
//        doNothing().when(cacheStore).delete(any());
//
//        Mono<CustomCredentialOffer> result = credentialOfferService.getCredentialOffer(id);
//
//        assertEquals(credentialOffer, result.block());
//        verify(cacheStore, times(1)).delete(id);
//    }
//
//    @Test
//    void testGetCredentialOfferTheReturnExpiredPreAuthorizedCodeException() {
//        // Mocking
//        String id = "nonExistentId";
//
//        when(cacheStore.get(any())).thenReturn(null);
//
//        assertThrows(ExpiredPreAuthorizedCodeException.class, () -> {
//            try {
//                credentialOfferService.getCredentialOffer(id).block();
//            } catch (Exception e) {
//                throw Exceptions.unwrap(e);
//            }
//        });
//
//        verify(cacheStore, times(1)).get(id);
//        verify(cacheStore, times(0)).delete(id);
//
//    }
//}
