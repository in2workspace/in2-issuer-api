package es.in2.issuer.backend.shared.infrastructure.config;

import es.in2.issuer.backend.shared.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.backend.shared.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.backend.shared.infrastructure.config.properties.AppProperties;
import es.in2.issuer.backend.shared.infrastructure.config.properties.CorsProperties;
import es.in2.issuer.backend.shared.infrastructure.config.properties.IssuerIdentityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static es.in2.issuer.backend.shared.domain.util.Constants.CREDENTIAL_OFFER_CACHE_EXPIRATION_TIME;
import static es.in2.issuer.backend.shared.domain.util.Constants.VERIFIABLE_CREDENTIAL_JWT_CACHE_EXPIRATION_TIME;
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
    private AppProperties appProperties;

    @Mock
    private IssuerIdentityProperties issuerIdentityProperties;
    @Mock
    private CorsProperties corsProperties;


    private AppConfig appConfig;

    @BeforeEach
    void setUp() {
        when(configAdapterFactory.getAdapter()).thenReturn(configAdapter);
        appConfig = new AppConfig(configAdapterFactory, appProperties, issuerIdentityProperties, corsProperties);
    }

    @Test
    void testGetIssuerBackendUrl() {
        // Arrange
        String expectedDomain = "https://api.example.com";
        when(appProperties.url()).thenReturn("api.external.url");
        when(configAdapter.getConfiguration("api.external.url")).thenReturn(expectedDomain);

        // Act
        String actualDomain = appConfig.getIssuerBackendUrl();

        // Assert
        assertEquals(expectedDomain, actualDomain);
    }

    @Test
    void testGetKnowledgeBaseUploadCertificationGuideUrl() {
        // Arrange
        String expectedUrl = "https://knowledge.example.com";
        AppProperties.KnowledgeBase knowledgeBase = mock(AppProperties.KnowledgeBase.class);
        when(appProperties.knowledgeBase()).thenReturn(knowledgeBase);
        when(knowledgeBase.uploadCertificationGuideUrl()).thenReturn("knowledge.base.wallet.url");
        when(configAdapter.getConfiguration("knowledge.base.wallet.url")).thenReturn(expectedUrl);

        // Act
        String actualUrl = appConfig.getKnowledgeBaseUploadCertificationGuideUrl();

        // Assert
        assertEquals(expectedUrl, actualUrl);
    }
    @Test
    void testGetIssuerFrontendUrl() {
        // Arrange
        String expectedDomain = "https://ui.example.com";
        when(appProperties.issuerFrontendUrl()).thenReturn("ui.external.url");
        when(configAdapter.getConfiguration("ui.external.url")).thenReturn(expectedDomain);

        // Act
        String actualDomain = appConfig.getIssuerFrontendUrl();

        // Assert
        assertEquals(expectedDomain, actualDomain);
    }

    @Test
    void testGetConfigSource() {
        // Arrange
        String expectedConfigSource = "configSourceValue";
        when(appProperties.configSource()).thenReturn("api.config.source");
        when(configAdapter.getConfiguration("api.config.source")).thenReturn(expectedConfigSource);

        // Act
        String actualConfigSource = appConfig.getConfigSource();

        // Assert
        assertEquals(expectedConfigSource, actualConfigSource);
    }

    @Test
    void testGetCacheLifetimeForCredentialOffer() {
        // Arrange
        long expectedLifetime = 10L;

        // Act
        long actualLifetime = CREDENTIAL_OFFER_CACHE_EXPIRATION_TIME;

        // Assert
        assertEquals(expectedLifetime, actualLifetime);
    }

    @Test
    void testGetCacheLifetimeForVerifiableCredential() {
        // Arrange
        long expectedLifetime = 10L;

        // Act
        long actualLifetime = VERIFIABLE_CREDENTIAL_JWT_CACHE_EXPIRATION_TIME;

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