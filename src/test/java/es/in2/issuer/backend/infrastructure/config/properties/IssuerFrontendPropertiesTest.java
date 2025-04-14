package es.in2.issuer.backend.infrastructure.config.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IssuerFrontendPropertiesTest {
    @Test
    void testIssuerUiProperties() {
        IssuerFrontendProperties issuerFrontendProperties = new IssuerFrontendProperties("url");

        assertEquals("url", issuerFrontendProperties.url());
    }
}