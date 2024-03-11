package es.in2.issuer.domain.service;

import es.in2.issuer.domain.service.impl.CredentialIssuerMetadataServiceImpl;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialIssuerMetadataServiceImplTest {

    @Mock
    private AppConfiguration appConfiguration;

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
//
//        new ServiceMatrix(Utils.SERVICE_MATRIX_PATH);
//
//        ReflectionTestUtils.setField(credentialIssuerMetadataService,"issuerApiBaseUrl","http://baseurl");
//        ReflectionTestUtils.setField(credentialIssuerMetadataService,"issuerUri","https://keycloak.example.com");
//        ReflectionTestUtils.setField(credentialIssuerMetadataService,"did","dummy");
//
//        List<CredentialsSupportedParameter> expectedCredentials = Arrays.asList(
//                new CredentialsSupportedParameter(
//                        "jwt_vc_json",
//                        "VerifiableId_JWT",
//                        Arrays.asList("VerifiableCredential", "VerifiableAttestation", "VerifiableId"),
//                        List.of("did"),
//                        List.of(),
//                        VcTemplateService.Companion.getService().getTemplate("VerifiableId", true, VcTemplateService.SAVED_VC_TEMPLATES_KEY)
//                ),
//                new CredentialsSupportedParameter(
//                        "jwt_vc_json",
//                        LEAR_CREDENTIAL,
//                        Arrays.asList("VerifiableCredential", "VerifiableAttestation", "LEARCredential"),
//                        List.of("did"),
//                        List.of(),
//                        null
//                )
//        );
//
//        CredentialIssuerMetadata result = credentialIssuerMetadataService.generateOpenIdCredentialIssuer().block();
//
//        assertNotNull(result);
//        assertEquals(expectedCredentials.size(), result.getCredentialsSupported().size());
//    }
}
