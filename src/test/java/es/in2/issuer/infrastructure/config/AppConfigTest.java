package es.in2.issuer.infrastructure.config;

import es.in2.issuer.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.infrastructure.config.properties.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppConfigTest {

    @Mock
    private ConfigAdapterFactory configAdapterFactory;

    @Mock
    private ConfigAdapter configAdapter;

    @Mock
    private ApiProperties apiProperties;

    @Mock
    private IssuerUiProperties issuerUiProperties;

    @Mock
    private WalletProperties walletProperties;
    @Mock
    private KnowledgeBaseProperties knowledgeBaseProperties;

    @Mock
    private IssuerIdentityProperties issuerIdentityProperties;


    private AppConfig appConfig;

    @BeforeEach
    void setUp() {
        when(configAdapterFactory.getAdapter()).thenReturn(configAdapter);
        appConfig = new AppConfig(configAdapterFactory, apiProperties, issuerUiProperties,walletProperties, issuerIdentityProperties, knowledgeBaseProperties);
    }

    @Test
    void testGetIssuerApiExternalDomain() {
        // Arrange
        String expectedDomain = "https://api.example.com";
        when(apiProperties.externalDomain()).thenReturn("api.external.domain");
        when(configAdapter.getConfiguration("api.external.domain")).thenReturn(expectedDomain);

        // Act
        String actualDomain = appConfig.getIssuerApiExternalDomain();

        // Assert
        assertEquals(expectedDomain, actualDomain);
    }

    @Test
    void testGetWalletUrl() {
        // Arrange
        String expectedUrl = "https://wallet.example.com";
        when(walletProperties.url()).thenReturn("wallet.url");
        when(configAdapter.getConfiguration("wallet.url")).thenReturn(expectedUrl);

        // Act
        String actualUrl = appConfig.getWalletUrl();

        // Assert
        assertEquals(expectedUrl, actualUrl);
    }
    @Test
    void testGetKnowledgeBaseUploadCertificationGuideUrl() {
        // Arrange
        String expectedUrl = "https://knowledge.example.com";
        when(knowledgeBaseProperties.uploadCertificationGuideUrl()).thenReturn("knowledge.base.url");
        when(configAdapter.getConfiguration("knowledge.base.url")).thenReturn(expectedUrl);

        // Act
        String actualUrl = appConfig.getKnowledgeBaseUploadCertificationGuideUrl();

        // Assert
        assertEquals(expectedUrl, actualUrl);
    }
    @Test
    void testGetIssuerUiExternalDomain() {
        // Arrange
        String expectedDomain = "https://ui.example.com";
        when(issuerUiProperties.externalDomain()).thenReturn("ui.external.domain");
        when(configAdapter.getConfiguration("ui.external.domain")).thenReturn(expectedDomain);

        // Act
        String actualDomain = appConfig.getIssuerUiExternalDomain();

        // Assert
        assertEquals(expectedDomain, actualDomain);
    }

    @Test
    void testGetApiConfigSource() {
        // Arrange
        String expectedConfigSource = "configSourceValue";
        when(apiProperties.configSource()).thenReturn("api.config.source");
        when(configAdapter.getConfiguration("api.config.source")).thenReturn(expectedConfigSource);

        // Act
        String actualConfigSource = appConfig.getApiConfigSource();

        // Assert
        assertEquals(expectedConfigSource, actualConfigSource);
    }

    @Test
    void testGetCacheLifetimeForCredentialOffer() {
        // Arrange
        long expectedLifetime = 3600L;
        ApiProperties.MemoryCache memoryCache = mock(ApiProperties.MemoryCache.class);
        when(apiProperties.cacheLifetime()).thenReturn(memoryCache);
        when(memoryCache.credentialOffer()).thenReturn(expectedLifetime);

        // Act
        long actualLifetime = appConfig.getCacheLifetimeForCredentialOffer();

        // Assert
        assertEquals(expectedLifetime, actualLifetime);
    }

    @Test
    void testGetCacheLifetimeForVerifiableCredential() {
        // Arrange
        long expectedLifetime = 7200L;
        ApiProperties.MemoryCache memoryCache = mock(ApiProperties.MemoryCache.class);
        when(apiProperties.cacheLifetime()).thenReturn(memoryCache);
        when(memoryCache.verifiableCredential()).thenReturn(expectedLifetime);

        // Act
        long actualLifetime = appConfig.getCacheLifetimeForVerifiableCredential();

        // Assert
        assertEquals(expectedLifetime, actualLifetime);
    }
}