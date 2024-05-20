package es.in2.issuer.infrastructure.configuration.adapter.yaml;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
