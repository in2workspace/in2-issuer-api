package es.in2.issuer.infrastructure.iam.adapter.keycloak;

import es.in2.issuer.infrastructure.config.AppConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class KeycloakIamAdapterTest {

    private KeycloakIamAdapter keycloakIamAdapter;

    @BeforeEach
    public void setUp() {
        // Assume AppConfiguration is properly instantiated or mocked
        AppConfiguration appConfiguration = mock(AppConfiguration.class);
        when(appConfiguration.getIamInternalDomain()).thenReturn("internal.example.com");
        when(appConfiguration.getIamExternalDomain()).thenReturn("external.example.com");
        when(appConfiguration.getJwtDecoderPath()).thenReturn("/path");
        when(appConfiguration.getJwtDecoderLocalPath()).thenReturn("/path");
        when(appConfiguration.getPreAuthCodeUriTemplate()).thenReturn("/path/{{did}}/path");
        when(appConfiguration.getTokenUriTemplate()).thenReturn("/path/{{did}}/token");
        when(appConfiguration.getIssuerDid()).thenReturn("dummyDid");

        keycloakIamAdapter = new KeycloakIamAdapter(appConfiguration);
    }

    @Test
    void testGetJwtDecoder() {
        String expectedUrl = "external.example.com/path";
        String actualUrl = keycloakIamAdapter.getJwtDecoder();
        assertEquals(expectedUrl, actualUrl, "The getJwtDecoder URL should match the expected value");
    }

    @Test
    void testGetJwtDecoderLocal() {
        String expectedUrl = "internal.example.com/path";
        String actualUrl = keycloakIamAdapter.getJwtDecoderLocal();
        assertEquals(expectedUrl, actualUrl, "The getJwtDecoderLocal Local URL should match the expected value");
    }

    @Test
    void testGetPreAuthCodeUri() {
        String expectedUrl = "internal.example.com/path/dummyDid/path";
        String actualUrl = keycloakIamAdapter.getPreAuthCodeUri();
        assertEquals(expectedUrl, actualUrl, "The getPreAuthCodeUri URL should match the expected value");
    }

    @Test
    void testGetTokenUri() {
        String expectedUrl = "internal.example.com/path/dummyDid/token";
        String actualUrl = keycloakIamAdapter.getTokenUri();
        assertEquals(expectedUrl, actualUrl, "The getTokenUri URL should match the expected value");
    }
}
