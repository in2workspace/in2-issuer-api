package es.in2.issuer.shared.infrastructure.config.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthServerPropertiesTest {

    @Test
    void testAuthServerProperties() {
        AuthServerProperties.Paths paths = new AuthServerProperties.Paths("issuerDid", "jwtDecoderPath", "jwtDecoderLocalPath", "jwtValidatorPath", "preAuthorizedCodePath", "tokenPath", "nonceValidationPath");
        AuthServerProperties.Client client = new AuthServerProperties.Client("clientId", "username", "password");
        AuthServerProperties authServerProperties = new AuthServerProperties("provider", "externalDomain", "internalDomain", "realm", paths, client);

        assertEquals("provider", authServerProperties.provider());
        assertEquals("externalDomain", authServerProperties.externalDomain());
        assertEquals("internalDomain", authServerProperties.internalDomain());
        assertEquals("realm", authServerProperties.realm());
        assertEquals(paths, authServerProperties.paths());
        assertEquals(client, authServerProperties.client());
    }
}