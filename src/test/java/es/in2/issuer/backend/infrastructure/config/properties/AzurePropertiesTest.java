package es.in2.issuer.backend.infrastructure.config.properties;

import es.in2.issuer.shared.infrastructure.config.properties.AzureProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AzurePropertiesTest {
    @Test
    void testAzureProperties() {
        AzureProperties.AzurePropertiesLabel azurePropertiesLabel = new AzureProperties.AzurePropertiesLabel("global");
        AzureProperties azureProperties = new AzureProperties("endpoint", azurePropertiesLabel);

        assertEquals("endpoint", azureProperties.endpoint());
        assertEquals(azurePropertiesLabel, azureProperties.label());
    }
}