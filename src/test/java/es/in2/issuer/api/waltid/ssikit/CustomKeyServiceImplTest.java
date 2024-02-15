package es.in2.issuer.api.waltid.ssikit;

import com.nimbusds.jose.jwk.ECKey;
import es.in2.issuer.api.util.Utils;
import es.in2.issuer.waltid.ssikit.impl.CustomKeyServiceImpl;
import id.walt.crypto.KeyAlgorithm;
import id.walt.crypto.KeyId;
import id.walt.servicematrix.ServiceMatrix;
import id.walt.services.key.KeyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.Objects;

import static com.nimbusds.jose.JWSAlgorithm.ES256K;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class CustomKeyServiceImplTest {

    @InjectMocks
    private CustomKeyServiceImpl customKeyService;
    @Test
    void generateKey_shouldGenerateKeySuccessfully() {

        // Act
        Mono<KeyId> result = customKeyService.generateKey();

        // Assert
        assertNotNull(result.block());
        assertEquals(32, Objects.requireNonNull(result.block()).getId().length());

    }
    @Test
    void getECKeyFromKid_shouldGetECKeySuccessfully() throws ParseException {

        new ServiceMatrix(Utils.SERVICE_MATRIX_PATH);

        // Arrange
        KeyId kid = KeyService.Companion.getService().generate(KeyAlgorithm.ECDSA_Secp256k1);

        // Act
        Mono<ECKey> result = customKeyService.getECKeyFromKid(kid.getId());

        // Assert
        assertNotNull(result.block());
        assertEquals(Objects.requireNonNull(result.block()).getAlgorithm().getName(),ES256K.getName());
    }

}
