package es.in2.issuer.backend.shared.infrastructure.config.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthServerPropertiesTest {

    @Test
    void testAuthServerProperties() {
        AuthServerProperties.Paths paths = new AuthServerProperties.Paths("issuerDid", "jwtDecoderPath", "jwtDecoderLocalPath", "jwtValidatorPath", "preAuthorizedCodePath", "tokenPath", "nonceValidationPath");
        AuthServerProperties.Client client = new AuthServerProperties.Client("clientId", "username", "password");
        AuthServerProperties authServerProperties = new AuthServerProperties("provider", "url", "internalUrl", "realm", paths, client);

        assertEquals("provider", authServerProperties.provider());
        assertEquals("url", authServerProperties.externalUrl());
        assertEquals("internalUrl", authServerProperties.internalUrl());
        assertEquals("realm", authServerProperties.realm());
        assertEquals(paths, authServerProperties.paths());
        assertEquals(client, authServerProperties.client());
    }
}