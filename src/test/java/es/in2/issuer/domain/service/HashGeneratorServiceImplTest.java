package es.in2.issuer.domain.service;

import es.in2.issuer.domain.service.impl.HashGeneratorServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class HashGeneratorServiceImplTest {
    @InjectMocks
    private HashGeneratorServiceImpl hashGeneratorService;

    private String testDocument;
    private String expectedSHA256Base64;

    @BeforeEach
    void setUp() throws Exception {
        testDocument = "Test Document";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(testDocument.getBytes(StandardCharsets.UTF_8));
        expectedSHA256Base64 = Base64.getEncoder().encodeToString(hashBytes);
    }

    @Test
    void testGenerateSHA256_Success() {
        String generatedHash = hashGeneratorService.generateSHA256(testDocument);
        Assertions.assertNotNull(generatedHash);
        Assertions.assertEquals(generatedHash, expectedSHA256Base64);
    }

    @Test
    void testGenerateSHA256_EmptyInput() {
        String generatedHash = hashGeneratorService.generateSHA256("");
        Assertions.assertNotNull(generatedHash);
        assertFalse(generatedHash.isEmpty());
    }

    @Test
    void testGenerateHash_ValidAlgorithm() {
        String generatedHash = hashGeneratorService.generateHash(testDocument, "2.16.840.1.101.3.4.2.1");

        Assertions.assertNotNull(generatedHash);
        Assertions.assertEquals(generatedHash, expectedSHA256Base64);
    }

    @Test
    void testGenerateHash_InvalidAlgorithm() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            hashGeneratorService.generateHash(testDocument, "1.3.6.1.4.1.11129.2.4.2");
        });

        Assertions.assertEquals("Error generating hash: algorithm not supported", exception.getMessage());
    }
}
