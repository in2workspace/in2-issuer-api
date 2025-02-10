package es.in2.issuer.infrastructure.config.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RemoteSignaturePropertiesTest {
    @Test
    void testRemoteSignatureProperties() {
        RemoteSignatureProperties.Paths paths = new RemoteSignatureProperties.Paths("signPath");
        RemoteSignatureProperties remoteSignatureProperties = new RemoteSignatureProperties("domain", paths, "clientId", "clientSecret", "credentialId", "credentialPassword", false);

        assertEquals("domain", remoteSignatureProperties.domain());
        assertEquals(paths, remoteSignatureProperties.paths());
        assertEquals("clientId", remoteSignatureProperties.clientId());
        assertEquals("clientSecret", remoteSignatureProperties.clientSecret());
        assertEquals("credentialId", remoteSignatureProperties.credentialId());
        assertEquals("credentialPassword", remoteSignatureProperties.credentialPassword());
    }
}