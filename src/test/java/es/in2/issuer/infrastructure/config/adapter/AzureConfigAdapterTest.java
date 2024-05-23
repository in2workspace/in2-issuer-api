package es.in2.issuer.infrastructure.config.adapter;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import es.in2.issuer.infrastructure.config.adapter.impl.AzureConfigAdapter;
import es.in2.issuer.infrastructure.config.properties.AzureProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AzureConfigAdapterTest {

    @Mock
    private ConfigurationClient configurationClient;

    private final AzureProperties.AzurePropertiesLabel azurePropertiesLabel = new AzureProperties.AzurePropertiesLabel("DummyLabel");

    private final AzureProperties azureProperties = new AzureProperties("DummyEndpoint", azurePropertiesLabel);

    private ConfigAdapter azureConfigAdapter;

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
