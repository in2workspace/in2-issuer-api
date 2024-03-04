package es.in2.issuer.api.config;
import es.in2.issuer.api.config.properties.AppConfigurationProperties;
import es.in2.issuer.configuration.service.GenericConfigAdapter;
import es.in2.issuer.configuration.util.ConfigAdapterFactory;
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
        when(appConfigurationProperties.keycloakExternalDomain()).thenReturn("keycloak.domain");
        when(genericConfigAdapter.getConfiguration("keycloak.domain")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getKeycloakExternalDomain());
    }

    @Test
    void testGetIssuerDomain() {
        String expected = "issuer-domain";
        when(appConfigurationProperties.issuerDomain()).thenReturn("issuer.domain");
        when(genericConfigAdapter.getConfiguration("issuer.domain")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getIssuerDomain());
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
    void testGetKeycloakDid() {
        String expected = "keycloak-did";
        when(appConfigurationProperties.keycloakDid()).thenReturn("keycloak.did");
        when(genericConfigAdapter.getConfiguration("keycloak.did")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getKeycloakDid());
    }

    @Test
    void testGetIssuerDid() {
        String expected = "issuer-did";
        when(appConfigurationProperties.issuerDid()).thenReturn("issuer.did");
        when(genericConfigAdapter.getConfiguration("issuer.did")).thenReturn(expected);
        assertEquals(expected, appConfiguration.getIssuerDid());
    }
}