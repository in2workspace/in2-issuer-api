package es.in2.issuer.backend.domain.util;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {
    @Test
    void testPrivateConstructorThrowsException() throws NoSuchMethodException {
        Constructor<Utils> constructor = Utils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException invocationTargetException = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertInstanceOf(IllegalStateException.class, invocationTargetException.getCause());
        assertEquals("Utility class", invocationTargetException.getCause().getMessage());
    }

    @Test
    void testGenerateCustomNonce() {
        StepVerifier.create(es.in2.issuer.shared.domain.util.Utils.generateCustomNonce())
                .assertNext(nonce -> {
                    assertNotNull(nonce);
                    assertFalse(nonce.isEmpty());
                    assertDoesNotThrow(() -> Base64.getUrlDecoder().decode(nonce));
                })
                .verifyComplete();
    }
}