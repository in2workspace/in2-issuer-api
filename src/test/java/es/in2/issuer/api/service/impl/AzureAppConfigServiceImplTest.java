package es.in2.issuer.api.service.impl;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.models.ConfigurationSetting;
import es.in2.issuer.api.config.azure.AzureAppConfigProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AzureAppConfigServiceImplTest {

    @Mock
    private ConfigurationClient azureConfigurationClient;

    @Mock
    private AzureAppConfigProperties azureAppConfigProperties;

    @InjectMocks
    private AzureAppConfigServiceImpl configService;

    @Test
    void getConfiguration_Success() {
        String key = "exampleKey";
        String expectedValue = "exampleValue";
        ReflectionTestUtils.setField(azureAppConfigProperties, "azureConfigLabel", "label");

        when(azureConfigurationClient.getConfigurationSetting(key, "label")).thenReturn(new ConfigurationSetting().setValue(expectedValue));

        Mono<String> resultMono = configService.getConfiguration(key);
        String result = resultMono.block();
        assertNotNull(result);
        assertEquals(expectedValue, result);

        verify(azureConfigurationClient, times(1)).getConfigurationSetting(key, "label");
    }

    @Test
    void getConfiguration_AppConfigCommunicationFailureNonExistingLabel() {
        String key = "errorKey";
        ReflectionTestUtils.setField(azureAppConfigProperties, "azureConfigLabel", "non-exist-label");
        when(azureConfigurationClient.getConfigurationSetting(key, "non-exist-label")).thenThrow(new RuntimeException(""));

        Mono<String> resultMono = configService.getConfiguration(key);
        String result = resultMono.block();
        assertNotNull(result);
        assertTrue(result.contains("Communication with AppConfiguration failed. Prefix or label not available"));

        verify(azureConfigurationClient, times(1)).getConfigurationSetting(key, "non-exist-label");
        verify(azureAppConfigProperties, never()).getAzureConfigLabel();
    }

    @Test
    void getConfiguration_AppConfigCommunicationFailureNullLabel() {
        String key = "errorKey";
        ReflectionTestUtils.setField(azureAppConfigProperties, "azureConfigLabel", null);
        when(azureConfigurationClient.getConfigurationSetting(key, "non-exist-label")).thenThrow(new RuntimeException(""));

        Mono<String> resultMono = configService.getConfiguration(key);
        String result = resultMono.block();
        assertNotNull(result);
        assertTrue(result.contains("Communication with AppConfiguration failed. Prefix or label not available"));

        verify(azureConfigurationClient, times(1)).getConfigurationSetting(key, null);
        verify(azureAppConfigProperties, never()).getAzureConfigLabel();
    }
}