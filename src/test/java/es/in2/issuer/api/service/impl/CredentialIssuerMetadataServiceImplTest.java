package es.in2.issuer.api.service.impl;

import es.in2.issuer.api.config.azure.AppConfigurationKeys;
import es.in2.issuer.api.model.dto.CredentialIssuerMetadata;
import es.in2.issuer.api.model.dto.CredentialsSupportedParameter;
import es.in2.issuer.api.service.AppConfigService;
import es.in2.issuer.api.service.AzureKeyVaultService;
import es.in2.issuer.api.util.Utils;
import id.walt.credentials.w3c.templates.VcTemplateService;
import id.walt.servicematrix.ServiceMatrix;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static es.in2.issuer.api.util.Constants.LEAR_CREDENTIAL;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialIssuerMetadataServiceImplTest {

    @Mock
    private AppConfigService appConfigService;

    @Mock
    private AzureKeyVaultService azureKeyVaultService;

    @InjectMocks
    private CredentialIssuerMetadataServiceImpl credentialIssuerMetadataService;

    @Test
    void testInitializeIssuerApiBaseUrl() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        lenient().when(appConfigService.getConfiguration(any())).thenReturn(Mono.just("dummyValue"));
        lenient().when(azureKeyVaultService.getSecretByKey(any())).thenReturn(Mono.just("dummySecret"));

        Method privateMethod = CredentialIssuerMetadataServiceImpl.class.getDeclaredMethod("initializeIssuerApiBaseUrl");
        privateMethod.setAccessible(true);

        privateMethod.invoke(credentialIssuerMetadataService);

        verify(appConfigService, times(1)).getConfiguration(AppConfigurationKeys.ISSUER_VCI_BASE_URL_KEY);
        verify(appConfigService, times(1)).getConfiguration(AppConfigurationKeys.KEYCLOAK_URI_KEY);
        verify(azureKeyVaultService, times(1)).getSecretByKey(AppConfigurationKeys.DID_ISSUER_KEYCLOAK_SECRET);
    }

    @Test
    void testInitializeAppConfigThrowsError() throws NoSuchMethodException {

        Method privateMethod = CredentialIssuerMetadataServiceImpl.class.getDeclaredMethod("initializeIssuerApiBaseUrl");
        privateMethod.setAccessible(true);

        lenient().when(appConfigService.getConfiguration(AppConfigurationKeys.ISSUER_VCI_BASE_URL_KEY)).thenReturn(Mono.error(new RuntimeException("Simulated error")));

        assertThrows(InvocationTargetException.class, () -> privateMethod.invoke(credentialIssuerMetadataService));

        verify(appConfigService, times(1)).getConfiguration(AppConfigurationKeys.ISSUER_VCI_BASE_URL_KEY);
        verify(appConfigService, times(0)).getConfiguration(AppConfigurationKeys.KEYCLOAK_URI_KEY);
        verify(azureKeyVaultService, times(0)).getSecretByKey(AppConfigurationKeys.DID_ISSUER_KEYCLOAK_SECRET);
    }

    @Test
    void testGetKeyVaultConfigurationWithError() throws NoSuchMethodException {

        Method privateMethod = CredentialIssuerMetadataServiceImpl.class.getDeclaredMethod("initializeIssuerApiBaseUrl");
        privateMethod.setAccessible(true);

        lenient().when(appConfigService.getConfiguration(any())).thenReturn(Mono.just("dummyValue"));
        lenient().when(azureKeyVaultService.getSecretByKey(any())).thenReturn(Mono.error(new RuntimeException("Simulated error")));

        assertThrows(InvocationTargetException.class, () -> privateMethod.invoke(credentialIssuerMetadataService));

        verify(appConfigService, times(1)).getConfiguration(AppConfigurationKeys.ISSUER_VCI_BASE_URL_KEY);
        verify(appConfigService, times(1)).getConfiguration(AppConfigurationKeys.KEYCLOAK_URI_KEY);
        verify(azureKeyVaultService, times(1)).getSecretByKey(AppConfigurationKeys.DID_ISSUER_KEYCLOAK_SECRET);
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

