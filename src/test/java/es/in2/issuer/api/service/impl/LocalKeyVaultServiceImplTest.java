package es.in2.issuer.api.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocalKeyVaultServiceImplTest {

    @Mock
    private Environment environment;

    @InjectMocks
    private LocalKeyVaultServiceImpl localKeyVaultService;

    @Test
    void getSecretByKey_ExistingKey_Success() {
        when(environment.getProperty("existingKey","")).thenReturn("existingValue");

        Mono<String> resultMono = localKeyVaultService.getSecretByKey("existingKey");
        String result = resultMono.block();

        // Verify that the result matches the expected value
        assertEquals("existingValue", result);
    }

    @Test
    void getSecretByKey_NonExistingKey_ReturnsEmptyString() {
        when(environment.getProperty("nonExistingKey","")).thenReturn("");

        Mono<String> resultMono = localKeyVaultService.getSecretByKey("nonExistingKey");
        String result = resultMono.block();

        // Verify that the result is an empty string for a non-existing key
        assertEquals("", result);
    }
}
