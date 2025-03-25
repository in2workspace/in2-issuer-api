package es.in2.issuer.backend.infrastructure.config.properties;

import es.in2.issuer.backend.infrastructure.config.properties.IssuerUiProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IssuerUiPropertiesTest {
    @Test
    void testIssuerUiProperties() {
        IssuerUiProperties issuerUiProperties = new IssuerUiProperties("externalDomain", "internalDomain");

        assertEquals("externalDomain", issuerUiProperties.externalDomain());
        assertEquals("internalDomain", issuerUiProperties.internalDomain());
    }
}