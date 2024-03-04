package es.in2.issuer.api.waltid.ssikit;

import es.in2.issuer.api.util.Utils;
import es.in2.issuer.waltid.ssikit.CustomDidService;
import es.in2.issuer.waltid.ssikit.CustomKeyService;
import es.in2.issuer.waltid.ssikit.impl.CustomDidServiceImpl;
import id.walt.crypto.KeyAlgorithm;
import id.walt.crypto.KeyId;
import id.walt.servicematrix.ServiceMatrix;
import id.walt.services.key.KeyService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomDidServiceImplTest {

    @Mock
    private CustomKeyService customKeyService;

    @InjectMocks
    private CustomDidServiceImpl customDidService;

    @Test
    void generateDidKey_shouldGenerateDidKey() {

        new ServiceMatrix(Utils.SERVICE_MATRIX_PATH);

        // Arrange
        KeyId kid = KeyService.Companion.getService().generate(KeyAlgorithm.ECDSA_Secp256k1);
        when(customKeyService.generateKey()).thenReturn(Mono.just(kid));
        //when(DidService.INSTANCE.create(DidMethod.key,kid.getId(), null)).thenReturn(anyString());


        // Act
        customDidService.generateDidKey().block();

        // Assert

        verify(customKeyService, times(1)).generateKey();
    }

    @Test
    void generateDidKeyWithKid_shouldGenerateDidKeyWithKid() {

        new ServiceMatrix(Utils.SERVICE_MATRIX_PATH);

        // Arrange
        KeyId kid = KeyService.Companion.getService().generate(KeyAlgorithm.ECDSA_Secp256k1);

        // Act
        String result = customDidService.generateDidKeyWithKid(kid.getId()).block();

        // Assert
        Assertions.assertEquals(57, Objects.requireNonNull(result).length());
    }

    @Test
    void generateDidKey_shouldHandleErrorInGenerateKey() {
        // Arrange
        when(customKeyService.generateKey()).thenReturn(Mono.error(new RuntimeException("Key generation failed")));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> handleGenerateDidKeyError(customDidService));

        // Verify
        verify(customKeyService, times(1)).generateKey();
    }

    private void handleGenerateDidKeyError(CustomDidService customDidService) {
        try {
            customDidService.generateDidKey().block();
        } catch (Exception e) {
            throw new RuntimeException("Error generating DID key", e);
        }
    }

}

