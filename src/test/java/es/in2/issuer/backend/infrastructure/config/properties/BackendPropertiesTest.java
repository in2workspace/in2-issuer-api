package es.in2.issuer.backend.infrastructure.config.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BackendPropertiesTest {

    @Test
    void testApiProperties() {
        BackendProperties backendProperties = new BackendProperties("url", "configSource");

        assertEquals("url", backendProperties.url());
        assertEquals("configSource", backendProperties.configSource());
    }

}