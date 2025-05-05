package es.in2.issuer.backend.oidc4vci.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorizationServerMetadataTest {

    @Test
    void testConstructorAndGetters() {
        // Arrange
        String issuer = "https://issuer.example.com";
        String tokenEndpoint = "https://issuer.example.com";
        Set<String> responseSupportedTypes = Set.of("token");
        boolean preAuthorizedGrantAnonymousAccessSupported = true;
        // Act
        AuthorizationServerMetadata authorizationServerMetadata = new AuthorizationServerMetadata(
                issuer, tokenEndpoint, responseSupportedTypes, preAuthorizedGrantAnonymousAccessSupported);
        // Assert
        assertEquals(issuer, authorizationServerMetadata.issuer());
        assertEquals(tokenEndpoint, authorizationServerMetadata.tokenEndpoint());
        assertEquals(responseSupportedTypes, authorizationServerMetadata.responseTypesSupported());
        assertTrue(authorizationServerMetadata.preAuthorizedGrantAnonymousAccessSupported());
    }

    @Test
    void testSetters() {
        // Arrange
        String issuer = "https://issuer.example.com";
        String tokenEndpoint = "https://issuer.example.com";
        Set<String> responseSupportedTypes = Set.of("token");
        boolean preAuthorizedGrantAnonymousAccessSupported = true;
        // Act
        AuthorizationServerMetadata authorizationServerMetadata = AuthorizationServerMetadata.builder()
                .issuer(issuer)
                .tokenEndpoint(tokenEndpoint)
                .responseTypesSupported(responseSupportedTypes)
                .preAuthorizedGrantAnonymousAccessSupported(preAuthorizedGrantAnonymousAccessSupported)
                .build();
        // Assert
        assertEquals(issuer, authorizationServerMetadata.issuer());
        assertEquals(tokenEndpoint, authorizationServerMetadata.tokenEndpoint());
        assertEquals(responseSupportedTypes, authorizationServerMetadata.responseTypesSupported());
        assertTrue(authorizationServerMetadata.preAuthorizedGrantAnonymousAccessSupported());
    }

}