package es.in2.issuer.infrastructure.config;

import es.in2.issuer.infrastructure.config.properties.AppConfigurationProperties;
import es.in2.issuer.infrastructure.configuration.service.GenericConfigAdapter;
import es.in2.issuer.infrastructure.configuration.util.ConfigAdapterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class AppConfigurationTest {

    @Mock
    private ConfigAdapterFactory configAdapterFactory;

    @Mock
    private AppConfigurationProperties appConfigurationProperties;

    @Mock
    private GenericConfigAdapter genericConfigAdapter;

    private AppConfiguration appConfiguration;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(configAdapterFactory.getAdapter()).thenReturn(genericConfigAdapter);
        appConfiguration = new AppConfiguration(configAdapterFactory, appConfigurationProperties);
    }

    @Test
    void testGetKeycloakDomain() {
        String expected = "keycloak-domain";
        when(appConfigurationProperties.iamInternalDomain()).thenReturn("keycloak.domain");
        when(genericConfigAdapter.getConfiguration("keycloak.domain")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getIamInternalDomain());
    }

    @Test
    void testGetIssuerDomain() {
        String expected = "issuer-domain";
        when(appConfigurationProperties.issuerExternalDomain()).thenReturn("issuer.domain");
        when(genericConfigAdapter.getConfiguration("issuer.domain")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getIssuerExternalDomain());
    }

    @Test
    void testGetAuthenticSourcesDomain() {
        String expected = "authentic-sources-domain";
        when(appConfigurationProperties.authenticSourcesDomain()).thenReturn("authentic.sources.domain");
        when(genericConfigAdapter.getConfiguration("authentic.sources.domain")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getAuthenticSourcesDomain());
    }

    @Test
    void testGetKeyVaultDomain() {
        String expected = "key-vault-domain";
        when(appConfigurationProperties.keyVaultDomain()).thenReturn("key.vault.domain");
        when(genericConfigAdapter.getConfiguration("key.vault.domain")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getKeyVaultDomain());
    }

    @Test
    void testGetRemoteSignatureDomain() {
        String expected = "remote-signature-domain";
        when(appConfigurationProperties.remoteSignatureDomain()).thenReturn("remote.signature.domain");
        when(genericConfigAdapter.getConfiguration("remote.signature.domain")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getRemoteSignatureDomain());
    }

    @Test
    void testGetIssuerDid() {
        String expected = "issuer-did";
        when(appConfigurationProperties.issuerDid()).thenReturn("issuer.did");
        when(genericConfigAdapter.getConfiguration("issuer.did")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getIssuerDid());
    }

    @Test
    void testGetJwtDecoderPath() {
        String expected = "jwt-decoder-path";
        when(appConfigurationProperties.jwtDecoderPath()).thenReturn("jwt.decoder.path");
        when(genericConfigAdapter.getConfiguration("jwt.decoder.path")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getJwtDecoderPath());
    }

    @Test
    void testGetJwtDecoderLocalPath() {
        String expected = "jwt-decoder-local-path";
        when(appConfigurationProperties.jwtDecoderLocalPath()).thenReturn("jwt.decoder.local.path");
        when(genericConfigAdapter.getConfiguration("jwt.decoder.local.path")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getJwtDecoderLocalPath());
    }

    @Test
    void testGetPreAuthCodeUriTemplate() {
        String expected = "pre-auth-code-uri-template";
        when(appConfigurationProperties.preAuthCodeUriTemplate()).thenReturn("pre.auth.code.uri.template");
        when(genericConfigAdapter.getConfiguration("pre.auth.code.uri.template")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getPreAuthCodeUriTemplate());
    }

    @Test
    void testGetTokenUriTemplate() {
        String expected = "token-uri-template";
        when(appConfigurationProperties.tokenUriTemplate()).thenReturn("token.uri.template");
        when(genericConfigAdapter.getConfiguration("token.uri.template")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getTokenUriTemplate());
    }

    @Test
    void testGetDbUser() {
        String expected = "db-user";
        when(appConfigurationProperties.dbUser()).thenReturn("db.user");
        when(genericConfigAdapter.getConfiguration("db.user")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getDbUser());
    }

    @Test
    void testGetDbPassword() {
        String expected = "db-password";
        when(appConfigurationProperties.dbPassword()).thenReturn("db.password");
        when(genericConfigAdapter.getConfiguration("db.password")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getDbPassword());
    }
    @Test
    void testGetDbHost() {
        String expected = "db-host";
        when(appConfigurationProperties.dbHost()).thenReturn("db.host");
        when(genericConfigAdapter.getConfiguration("db.host")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getDbHost());
    }

    @Test
    void testGetDbPort() {
        int expected = 1234; // Use int since getDbPort() returns an int
        when(appConfigurationProperties.dbPort()).thenReturn("db.port");
        when(genericConfigAdapter.getConfiguration("db.port")).thenReturn(Integer.toString(expected));
        assertEquals(expected, appConfiguration.getDbPort());
    }

    @Test
    void testGetDbName() {
        String expected = "db-name";
        when(appConfigurationProperties.dbName()).thenReturn("db.name");
        when(genericConfigAdapter.getConfiguration("db.name")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getDbName());
    }
}
