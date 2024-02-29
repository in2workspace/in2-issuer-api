package es.in2.issuer.configuration.adapter.azure.config;

import com.azure.core.credential.TokenCredential;
import com.azure.data.appconfiguration.ConfigurationClient;
import es.in2.issuer.configuration.adapter.azure.config.properties.AzureProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AzureAppConfigurationConfigTest {
    @Mock
    private AzureProperties azureProperties;

    @InjectMocks
    private AzureAppConfigurationConfig azureAppConfigurationConfig;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAzureTokenCredential() {
        // Execution
        TokenCredential tokenCredential = azureAppConfigurationConfig.azureTokenCredential();

        // Verification
        assertNotNull(tokenCredential, "TokenCredential should not be null");
        verifyNoInteractions(azureProperties); // Ensures azureProperties is not used in this bean creation
    }

    @Test
    public void testAzureConfigurationClient() {
        // Setup
        TokenCredential tokenCredential = mock(TokenCredential.class);
        when(azureProperties.endpoint()).thenReturn("https://your-endpoint-here");

        // Execution
        ConfigurationClient configurationClient = azureAppConfigurationConfig.azureConfigurationClient(tokenCredential);

        // Verification
        assertNotNull(configurationClient, "ConfigurationClient should not be null");
        verify(azureProperties, times(2)).endpoint(); // Adjusted to expect 2 invocations
    }
}