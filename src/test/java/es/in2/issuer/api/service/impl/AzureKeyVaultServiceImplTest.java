package es.in2.issuer.api.service.impl;


import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import es.in2.issuer.vault.AzureKeyVaultServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AzureKeyVaultServiceImplTest {

    @Mock
    private SecretClient secretClient;

    @InjectMocks
    private AzureKeyVaultServiceImpl azureKeyVaultService;

    @Test
    void getSecretByKey_ExistingKey_Success() {

        when(secretClient.getSecret("existingKey"))
                .thenReturn(new KeyVaultSecret("existingKey", "existingSecret"));

        Mono<String> resultMono = azureKeyVaultService.getSecretByKey("existingKey");
        String result = resultMono.block();

        // Verify that the result matches the expected value
        assertEquals("existingSecret", result);

        // Verify that the SecretClient method was called once with the correct key
        verify(secretClient, times(1)).getSecret("existingKey");
    }

    @Test
    void getSecretByKey_NonExistingKey_ThrowsException() {

        when(secretClient.getSecret("nonExistingKey"))
                .thenThrow(new RuntimeException("Secret not found"));

        Mono<String> resultMono = azureKeyVaultService.getSecretByKey("nonExistingKey");

        // Verify that the result contains the expected exception message
        Exception exception = assertThrows(Exception.class, resultMono::block);
        Assertions.assertThat(exception.getMessage().contains("Communication with AppConfiguration failed. KeyVault not available"));

        // Verify that the SecretClient method was called once with the correct key
        verify(secretClient, times(1)).getSecret("nonExistingKey");
    }

}
