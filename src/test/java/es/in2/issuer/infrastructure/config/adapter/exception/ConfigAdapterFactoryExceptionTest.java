package es.in2.issuer.infrastructure.config.adapter.exception;

import es.in2.issuer.backend.infrastructure.config.adapter.exception.ConfigAdapterFactoryException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigAdapterFactoryExceptionTest {
    @Test
    void testConstructor() {
        int size = 5;
        ConfigAdapterFactoryException exception = new ConfigAdapterFactoryException(size);
        assertEquals("Error creating ConfigAdapterFactory. There should be only one ConfigAdapter. Found: " + size, exception.getMessage());
    }
}