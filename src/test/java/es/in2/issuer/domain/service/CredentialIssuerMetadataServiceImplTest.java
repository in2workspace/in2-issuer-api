package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.CredentialIssuerMetadata;
import es.in2.issuer.domain.model.CredentialsSupported;
import es.in2.issuer.domain.model.VcTemplate;
import es.in2.issuer.domain.service.impl.CredentialIssuerMetadataServiceImpl;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.config.SecurityConfig;
import es.in2.issuer.infrastructure.iam.service.GenericIamAdapter;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialIssuerMetadataServiceImplTest {

    @Mock
    private AppConfiguration appConfiguration;

    @Mock
    private IamAdapterFactory iamAdapterFactory;

    @Mock
    private GenericIamAdapter genericIamAdapter;

    @InjectMocks
    private CredentialIssuerMetadataServiceImpl credentialIssuerMetadataService;


    @Test
    void testInitializeIssuerApiBaseUrl() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        lenient().when(appConfiguration.getIssuerDomain()).thenReturn(String.valueOf(Mono.just("dummyValue")));

        Method privateMethod = CredentialIssuerMetadataServiceImpl.class.getDeclaredMethod("initializeIssuerApiBaseUrl");
        privateMethod.setAccessible(true);

        privateMethod.invoke(credentialIssuerMetadataService);

        verify(appConfiguration, times(1)).getIssuerDomain();

    }

    @Test
    void testInitializeAppConfigThrowsError() throws NoSuchMethodException {
        Method privateMethod = CredentialIssuerMetadataServiceImpl.class.getDeclaredMethod("initializeIssuerApiBaseUrl");
        privateMethod.setAccessible(true);

        when(appConfiguration.getIssuerDomain()).thenAnswer(invocation -> Mono.error(new RuntimeException("Simulated error")));

        assertThrows(InvocationTargetException.class, () -> privateMethod.invoke(credentialIssuerMetadataService));

        verify(appConfiguration, times(1)).getIssuerDomain();
    }

    // TODO : Mock waltid services call

//    @Test
//    void generateOpenIdCredentialIssuerTest() {
//        // Mock the getTokenUri() response
//        String mockTokenUri = "http://mocktokenuri";
//        when(iamAdapterFactory.getAdapter()).thenReturn(genericIamAdapter);
//        when(genericIamAdapter.getTokenUri()).thenReturn(mockTokenUri);
//
//        ReflectionTestUtils.setField(credentialIssuerMetadataService,"issuerApiBaseUrl","http://baseurl");
//
//        List<CredentialsSupported> expectedCredentials = Arrays.asList(
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
//        CredentialIssuerMetadata result = credentialIssuerMetadataService.generateOpenIdCredentialIssuer().block();
//
//        Assertions.assertNotNull(result);
//        Assertions.assertEquals(expectedCredentials.size(), result.credentialsSupported().size());
//    }
}
