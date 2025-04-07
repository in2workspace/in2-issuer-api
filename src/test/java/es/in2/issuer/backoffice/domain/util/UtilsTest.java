package es.in2.issuer.backoffice.domain.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
}