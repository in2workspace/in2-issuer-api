package es.in2.issuer.shared.infrastructure.config;

import es.in2.issuer.shared.infrastructure.config.properties.IssuerIdentityProperties;
import es.in2.issuer.shared.infrastructure.config.properties.IssuerUiProperties;
import es.in2.issuer.shared.infrastructure.config.properties.KnowledgeBaseProperties;
import es.in2.issuer.shared.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.shared.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.shared.infrastructure.config.properties.ApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private KnowledgeBaseProperties knowledgeBaseProperties;

    @Mock
    private IssuerUiProperties issuerUiProperties;

    @Mock
    private IssuerIdentityProperties issuerIdentityProperties;

    @InjectMocks
    private AppConfig appConfig;

    @BeforeEach
    void setUp() {
        when(configAdapterFactory.getAdapter()).thenReturn(configAdapter);
        appConfig = new AppConfig(configAdapterFactory, apiProperties, knowledgeBaseProperties, issuerUiProperties, issuerIdentityProperties);
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

}