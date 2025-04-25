package es.in2.issuer.backend.shared.domain.util;

import es.in2.issuer.backend.shared.domain.util.EndpointsConstants;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EndpointsConstantsTest {
    @Test
    void testPrivateConstructorThrowsException() throws NoSuchMethodException {
        Constructor<EndpointsConstants> constructor = EndpointsConstants.class.getDeclaredConstructor();

        constructor.setAccessible(true);

        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }
}