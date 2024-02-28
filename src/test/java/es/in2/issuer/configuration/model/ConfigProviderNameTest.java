package es.in2.issuer.configuration.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigProviderNameTest {
    @Test
    public void testToString() {
        assertEquals("azure", ConfigProviderName.AZURE.toString());
        assertEquals("yaml", ConfigProviderName.YAML.toString());
    }

    @Test
    public void testEnumValues() {
        ConfigProviderName[] values = ConfigProviderName.values();
        assertEquals(2, values.length);
        assertEquals(ConfigProviderName.AZURE, values[0]);
        assertEquals(ConfigProviderName.YAML, values[1]);
    }
}