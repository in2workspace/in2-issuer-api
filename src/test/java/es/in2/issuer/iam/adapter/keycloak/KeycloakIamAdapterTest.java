package es.in2.issuer.iam.adapter.keycloak;

import es.in2.issuer.api.config.AppConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakIamAdapterTest {

    private KeycloakIamAdapter keycloakIamAdapter;

    @BeforeEach
    void setUp() {
        // Mock AppConfiguration
        AppConfiguration appConfiguration = mock(AppConfiguration.class);
        // Setup mock to return "example.com" for internal and external domains, and a dummy DID
        when(appConfiguration.getIamInternalDomain()).thenReturn("internal.example.com");
        when(appConfiguration.getIamExternalDomain()).thenReturn("external.example.com");
        when(appConfiguration.getIamDid()).thenReturn("dummyDid");

        // Initialize KeycloakIamAdapter with the mocked AppConfiguration
        keycloakIamAdapter = new KeycloakIamAdapter(appConfiguration);
        // Manually invoke the initialization method (since @PostConstruct won't be called in this test setup)
        keycloakIamAdapter.initializeKeycloakIamAdapter();
    }

    @Test
    void testGetJwtDecoder() {
        String expectedUrl = "https://external.example.com/realms/EAAProvider/protocol/openid-connect/certs";
        assertEquals(expectedUrl, keycloakIamAdapter.getJwtDecoder());
    }

    @Test
    void testGetJwtDecoderLocal() {
        String expectedUrl = "internal.example.com/realms/EAAProvider";
        assertEquals(expectedUrl, keycloakIamAdapter.getJwtDecoderLocal());
    }

    @Test
    void testGetPreAuthCodeUri() {
        String expectedUrl = "internal.example.com/realms/EAAProvider/verifiable-credential/dummyDid/credential-offer";
        assertEquals(expectedUrl, keycloakIamAdapter.getPreAuthCodeUri());
    }

    @Test
    void testGetTokenUri() {
        String expectedUrl = "internal.example.com/realms/EAAProvider/verifiable-credential/dummyDid/token";
        assertEquals(expectedUrl, keycloakIamAdapter.getTokenUri());
    }
}