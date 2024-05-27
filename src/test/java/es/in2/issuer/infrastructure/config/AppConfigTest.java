package es.in2.issuer.infrastructure.config;

import es.in2.issuer.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.infrastructure.config.properties.ApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.when;

class AppConfigTest {

    @Mock
    private ConfigAdapterFactory configAdapterFactory;

    @Mock
    private ApiProperties apiProperties;

    @Mock
    private ConfigAdapter configAdapter;

    private AppConfig appConfig;

//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//        when(configAdapterFactory.getAdapter()).thenReturn(configAdapter);
//        appConfig = new AppConfig(configAdapterFactory, apiProperties);
//    }

//    @Test
//    void testGetKeycloakDomain() {
//        String expected = "keycloak-domain";
//        when(apiProperties.iamInternalDomain()).thenReturn("keycloak.domain");
//        when(configAdapter.getConfiguration("keycloak.domain")).thenReturn(expected);
//        assertEquals(expected, apiConfig.getIamInternalDomain());
//    }

//    @Test
//    void testGetIssuerDomain() {
//        String expected = "issuer-domain";
//        when(apiProperties.issuerExternalDomain()).thenReturn("issuer.domain");
//        when(configAdapter.getConfiguration("issuer.domain")).thenReturn(expected);
//        assertEquals(expected, apiConfig.getIssuerExternalDomain());
//    }

//    @Test
//    void testGetAuthenticSourcesDomain() {
//        String expected = "authentic-sources-domain";
//        when(apiProperties.authenticSourcesDomain()).thenReturn("authentic.sources.domain");
//        when(configAdapter.getConfiguration("authentic.sources.domain")).thenReturn(expected);
//        assertEquals(expected, apiConfig.getAuthenticSourcesDomain());
//    }

//    @Test
//    void testGetKeyVaultDomain() {
//        String expected = "key-vault-domain";
//        when(apiProperties.keyVaultDomain()).thenReturn("key.vault.domain");
//        when(configAdapter.getConfiguration("key.vault.domain")).thenReturn(expected);
//        assertEquals(expected, apiConfig.getKeyVaultDomain());
//    }

//    @Test
//    void testGetRemoteSignatureDomain() {
//        String expected = "remote-signature-domain";
//        when(apiProperties.remoteSignatureDomain()).thenReturn("remote.signature.domain");
//        when(configAdapter.getConfiguration("remote.signature.domain")).thenReturn(expected);
//        assertEquals(expected, apiConfig.getRemoteSignatureDomain());
//    }

//    @Test
//    void testGetIssuerDid() {
//        String expected = "issuer-did";
//        when(apiProperties.issuerDid()).thenReturn("issuer.did");
//        when(configAdapter.getConfiguration("issuer.did")).thenReturn(expected);
//        assertEquals(expected, apiConfig.getIssuerDid());
//    }

//    @Test
//    void testGetJwtDecoderPath() {
//        String expected = "jwt-decoder-path";
//        when(apiProperties.jwtDecoderPath()).thenReturn("jwt.decoder.path");
//        when(configAdapter.getConfiguration("jwt.decoder.path")).thenReturn(expected);
//        assertEquals(expected, apiConfig.getJwtDecoderPath());
//    }

//    @Test
//    void testGetJwtDecoderLocalPath() {
//        String expected = "jwt-decoder-local-path";
//        when(apiProperties.jwtDecoderLocalPath()).thenReturn("jwt.decoder.local.path");
//        when(configAdapter.getConfiguration("jwt.decoder.local.path")).thenReturn(expected);
//        assertEquals(expected, apiConfig.getJwtDecoderLocalPath());
//    }

//    @Test
//    void testGetPreAuthCodeUriTemplate() {
//        String expected = "pre-auth-code-uri-template";
//        when(apiProperties.preAuthCodeUriTemplate()).thenReturn("pre.auth.code.uri.template");
//        when(configAdapter.getConfiguration("pre.auth.code.uri.template")).thenReturn(expected);
//        assertEquals(expected, apiConfig.getPreAuthCodeUriTemplate());
//    }

//    @Test
//    void testGetTokenUriTemplate() {
//        String expected = "token-uri-template";
//        when(apiProperties.tokenUriTemplate()).thenReturn("token.uri.template");
//        when(configAdapter.getConfiguration("token.uri.template")).thenReturn(expected);
//        assertEquals(expected, apiConfig.getTokenUriTemplate());
//    }

}
