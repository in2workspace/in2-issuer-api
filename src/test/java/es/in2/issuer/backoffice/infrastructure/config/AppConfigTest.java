package es.in2.issuer.backoffice.infrastructure.config;

import es.in2.issuer.backoffice.infrastructure.config.properties.CorsProperties;
import es.in2.issuer.shared.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.shared.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.shared.infrastructure.config.properties.ApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    private CorsProperties corsProperties;


    private AppConfig appConfig;

    @BeforeEach
    void setUp() {
        when(configAdapterFactory.getAdapter()).thenReturn(configAdapter);
        appConfig = new AppConfig(configAdapterFactory, apiProperties, corsProperties);
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