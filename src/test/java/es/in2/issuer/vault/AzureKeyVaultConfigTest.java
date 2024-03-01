package es.in2.issuer.vault;

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import es.in2.issuer.api.config.AppConfiguration;
import es.in2.issuer.api.exception.AzureConfigurationSettingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AzureKeyVaultConfigTest {

    @Mock
    private AppConfiguration appConfiguration;

    @Mock
    private TokenCredential tokenCredential;

    @InjectMocks
    private AzureKeyVaultConfig azureKeyVaultConfig;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Initialize mocks
    }

    @Test
    void whenKeyVaultDomainIsNotNull_thenSecretClientShouldBeReturned() throws AzureConfigurationSettingException {
        // Arrange
        String keyVaultEndpoint = "https://example-key-vault.vault.azure.net/";
        when(appConfiguration.getKeyVaultDomain()).thenReturn(keyVaultEndpoint);

        // Act
        SecretClient result = azureKeyVaultConfig.secretClient(tokenCredential);

        // Assert
        assertNotNull(result, "SecretClient should not be null");
    }

    @Test
    void whenKeyVaultDomainIsNull_thenThrowAzureConfigurationSettingException() {
        // Arrange
        when(appConfiguration.getKeyVaultDomain()).thenReturn(null);

        // Act & Assert
        assertThrows(AzureConfigurationSettingException.class, () -> azureKeyVaultConfig.secretClient(tokenCredential),
                "Expected AzureConfigurationSettingException to be thrown, but it was not");
    }
}