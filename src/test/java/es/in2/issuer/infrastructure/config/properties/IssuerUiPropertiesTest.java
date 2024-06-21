package es.in2.issuer.infrastructure.config.properties;

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