package es.in2.issuer.infrastructure.iam.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IamProviderNameTest {

    @Test
    void testToString() {
        assertEquals("keycloak", IamProviderName.KEYCLOAK.toString());
    }

    @Test
    void testEnumValues() {
        IamProviderName[] values = IamProviderName.values();
        assertEquals(2, values.length);
        assertEquals(IamProviderName.KEYCLOAK.toString(), values[0].toString());
    }

}
