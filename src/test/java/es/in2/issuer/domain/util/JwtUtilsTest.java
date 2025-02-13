package es.in2.issuer.domain.util;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Base64;

@ExtendWith(MockitoExtension.class)
public class JwtUtilsTest {

    private final JwtUtils jwtUtils = new JwtUtils();

    @Test
    void testGetPayload() {
        String jwt = "header.payload.signature";
        String payload = jwtUtils.getPayload(jwt);
        assertEquals("payload", payload, "El payload extraído coincide");
    }

    @Test
    void testDecodePayload() {
        String jsonPayload = "{\"sub\":\"1234567890\",\"name\":\"John Doe\",\"iat\":1516239022}";
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(jsonPayload.getBytes());
        String jwt = "header." + encodedPayload + ".signature";
        String decodedPayload = jwtUtils.decodePayload(jwt);
        assertEquals(jsonPayload, decodedPayload, "El payload decodificado coincide");
    }

    @Test
    void testGetPayloadWithInvalidJWT() {
        String invalidJwt = "headeronly";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> jwtUtils.getPayload(invalidJwt));

        assertEquals("invalid JWT", exception.getMessage());
    }

    @Test
    void testDecodePayloadWithInvalidJWT() {
        String invalidJwt = "headeronly";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> jwtUtils.decodePayload(invalidJwt));

        assertEquals("invalid JWT", exception.getMessage());
    }

    @Test
    void testGetPayloadMocked() {
        JwtUtils mockJwtUtils = mock(JwtUtils.class);
        when(mockJwtUtils.getPayload("header.payload.signature")).thenReturn("payload");
        assertEquals("payload", mockJwtUtils.getPayload("header.payload.signature"));
        verify(mockJwtUtils, times(1)).getPayload("header.payload.signature");
    }
}
