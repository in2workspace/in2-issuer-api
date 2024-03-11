package es.in2.issuer.infrastructure.iam.adapter.keycloak;

import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.iam.adapter.keycloak.KeycloakIamAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakIamAdapterTest {

    private KeycloakIamAdapter keycloakIamAdapter;

    @BeforeEach
    public void setUp() throws Exception {
        // Assume AppConfiguration is properly instantiated or mocked
        AppConfiguration appConfiguration = mock(AppConfiguration.class);
        when(appConfiguration.getIamInternalDomain()).thenReturn("internal.example.com");
        when(appConfiguration.getIamExternalDomain()).thenReturn("external.example.com");
        when(appConfiguration.getIamDid()).thenReturn("dummyDid");

        keycloakIamAdapter = new KeycloakIamAdapter(appConfiguration);

        // Using reflection to invoke the private method
        Method method = KeycloakIamAdapter.class.getDeclaredMethod("initializeKeycloakIamAdapter");
        method.setAccessible(true); // This line enables access to the private method
        method.invoke(keycloakIamAdapter); // Invoke the method on the instance of KeycloakIamAdapter
    }

    @Test
    void testGetJwtDecoder() {
        String expectedUrl = "https://external.example.com/realms/EAAProvider/protocol/openid-connect/certs";
        String actualUrl = keycloakIamAdapter.getJwtDecoder();
        assertEquals(expectedUrl, actualUrl, "The getJwtDecoder URL should match the expected value");
    }

    @Test
    void testGetJwtDecoderLocal() {
        String expectedUrl = "internal.example.com/realms/EAAProvider";
        String actualUrl = keycloakIamAdapter.getJwtDecoderLocal();
        assertEquals(expectedUrl, actualUrl, "The getJwtDecoderLocal Local URL should match the expected value");
    }

    @Test
    void testGetPreAuthCodeUri() {
        String expectedUrl = "internal.example.com/realms/EAAProvider/verifiable-credential/dummyDid/credential-offer";
        String actualUrl = keycloakIamAdapter.getPreAuthCodeUri();
        assertEquals(expectedUrl, actualUrl, "The getPreAuthCodeUri URL should match the expected value");
    }

    @Test
    void testGetTokenUri() {
        String expectedUrl = "internal.example.com/realms/EAAProvider/verifiable-credential/dummyDid/token";
        String actualUrl = keycloakIamAdapter.getTokenUri();
        assertEquals(expectedUrl, actualUrl, "The getTokenUri URL should match the expected value");
    }
}
