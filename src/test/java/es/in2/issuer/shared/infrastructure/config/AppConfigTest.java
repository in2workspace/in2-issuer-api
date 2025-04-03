package es.in2.issuer.shared.infrastructure.config;

import es.in2.issuer.backend.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.backend.infrastructure.config.adapter.factory.ConfigAdapterFactory;
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

    @InjectMocks
    private AppConfig appConfig;

    @BeforeEach
    void setUp() {
        when(configAdapterFactory.getAdapter()).thenReturn(configAdapter);
        appConfig = new AppConfig(configAdapterFactory, apiProperties);
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

}