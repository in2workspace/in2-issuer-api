package es.in2.issuer.configuration.adapter.azure;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import es.in2.issuer.configuration.adapter.azure.config.properties.AzureProperties;
import es.in2.issuer.configuration.adapter.azure.config.properties.AzurePropertiesLabel;
import es.in2.issuer.configuration.service.GenericConfigAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AzureConfigAdapterTest {
    @Mock
    private ConfigurationClient configurationClient;

    private final AzurePropertiesLabel azurePropertiesLabel = new AzurePropertiesLabel("DummyLabel");

    private final AzureProperties azureProperties = new AzureProperties("DummyEndpoint", azurePropertiesLabel);

    private GenericConfigAdapter azureConfigAdapter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        azureConfigAdapter = new AzureConfigAdapter(configurationClient, azureProperties);
    }

    @Test
    void testGetConfiguration() {
        // Mock the response from ConfigurationClient
        String key = "testKey";
        String expectedValue = "testValue";
        ConfigurationSetting configurationSetting = new ConfigurationSetting().setValue(expectedValue);
        when(configurationClient.getConfigurationSetting(anyString(), anyString())).thenReturn(configurationSetting);

        // Call the method under test
        String actualValue = azureConfigAdapter.getConfiguration(key);

        // Verify the interaction and the result
        assertEquals(expectedValue, actualValue);
    }
}