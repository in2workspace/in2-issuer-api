package es.in2.issuer.backend.infrastructure.config.adapter.factory;

import es.in2.issuer.backend.infrastructure.config.AppConfig;
import es.in2.issuer.backend.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.backend.infrastructure.config.adapter.impl.AzureConfigAdapter;
import es.in2.issuer.backend.infrastructure.config.adapter.impl.YamlConfigAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigAdapterFactoryTest {

    @Mock
    private AppConfig appConfig;

    @Mock
    private AzureConfigAdapter azureConfigAdapter;

    @Mock
    private YamlConfigAdapter yamlConfigAdapter;

    @InjectMocks
    private ConfigAdapterFactory configAdapterFactory;

    @Test
    void getAdapter_AzureConfig() {
        //Arrange
        when(appConfig.getConfigSource()).thenReturn("azure");

        // Act
        ConfigAdapter configAdapter = configAdapterFactory.getAdapter();

        // Assert
        assertEquals(azureConfigAdapter, configAdapter);
    }

    @Test
    void getAdapter_YamlConfig() {
        //Arrange
        when(appConfig.getConfigSource()).thenReturn("yaml");

        // Act
        ConfigAdapter configAdapter = configAdapterFactory.getAdapter();

        // Assert
        assertEquals(yamlConfigAdapter, configAdapter);
    }

    @Test
    void getAdapter_InvalidConfig() {
        //Arrange
        when(appConfig.getConfigSource()).thenReturn("invalid");

        // Act & Assert
        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class,
                () -> configAdapterFactory.getAdapter()
        );

        assertEquals("Invalid Config Adapter Provider: invalid", illegalArgumentException.getMessage());
    }
}