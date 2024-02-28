package es.in2.issuer.configuration.adapter.yaml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class YamlConfigAdapterTest {
    @Test
    void getConfiguration_returnsKey() {
        // Arrange
        YamlConfigAdapter yamlConfigAdapter = new YamlConfigAdapter();
        String key = "testKey";

        // Act
        String result = yamlConfigAdapter.getConfiguration(key);

        // Assert
        assertEquals(key, result);
    }
}