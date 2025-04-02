package es.in2.issuer.backend.infrastructure.config;

import es.in2.issuer.backend.infrastructure.config.AppConfig;
import es.in2.issuer.backend.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.backend.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.backend.infrastructure.config.properties.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private KnowledgeBaseProperties knowledgeBaseProperties;

    @Mock
    private IssuerIdentityProperties issuerIdentityProperties;
    @Mock
    private CorsProperties corsProperties;


    private AppConfig appConfig;

    @BeforeEach
    void setUp() {
        when(configAdapterFactory.getAdapter()).thenReturn(configAdapter);
        appConfig = new AppConfig(configAdapterFactory, apiProperties, issuerUiProperties, issuerIdentityProperties, knowledgeBaseProperties, corsProperties);
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
    void testGetKnowledgeBaseUploadCertificationGuideUrl() {
        // Arrange
        String expectedUrl = "https://knowledge.example.com";
        when(knowledgeBaseProperties.uploadCertificationGuideUrl()).thenReturn("knowledge.base.wallet.url");
        when(configAdapter.getConfiguration("knowledge.base.wallet.url")).thenReturn(expectedUrl);

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

    @Test
    void getExternalCorsAllowedOrigins_returnsConfiguredOrigins() {
        List<String> expectedOrigins = List.of("https://example.com", "https://another.com");
        when(corsProperties.externalAllowedOrigins()).thenReturn(expectedOrigins);

        List<String> actualOrigins = appConfig.getExternalCorsAllowedOrigins();

        assertEquals(expectedOrigins, actualOrigins);
    }

    @Test
    void getDefaultCorsAllowedOrigins_returnsConfiguredOrigins() {
        List<String> expectedOrigins = List.of("https://default.com", "https://default2.com");
        when(corsProperties.defaultAllowedOrigins()).thenReturn(expectedOrigins);

        List<String> actualOrigins = appConfig.getDefaultCorsAllowedOrigins();

        assertEquals(expectedOrigins, actualOrigins);
    }

    @Test
    void getExternalCorsAllowedOrigins_whenNoOriginsConfigured_returnsEmptyList() {
        when(corsProperties.externalAllowedOrigins()).thenReturn(Collections.emptyList());

        List<String> actualOrigins = appConfig.getExternalCorsAllowedOrigins();

        assertTrue(actualOrigins.isEmpty());
    }

    @Test
    void getDefaultCorsAllowedOrigins_whenNoOriginsConfigured_returnsEmptyList() {
        when(corsProperties.defaultAllowedOrigins()).thenReturn(Collections.emptyList());

        List<String> actualOrigins = appConfig.getDefaultCorsAllowedOrigins();

        assertTrue(actualOrigins.isEmpty());
    }
}