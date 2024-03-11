package es.in2.issuer.infrastructure.configuration.model;

import es.in2.issuer.infrastructure.configuration.model.ConfigProviderName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConfigProviderNameTest {

    @Test
    void testToString() {
        assertEquals("azure", ConfigProviderName.AZURE.toString());
        assertEquals("yaml", ConfigProviderName.YAML.toString());
    }

    @Test
    void testEnumValues() {
        ConfigProviderName[] values = ConfigProviderName.values();
        assertEquals(2, values.length);
        assertEquals(ConfigProviderName.AZURE, values[0]);
        assertEquals(ConfigProviderName.YAML, values[1]);
    }
}
