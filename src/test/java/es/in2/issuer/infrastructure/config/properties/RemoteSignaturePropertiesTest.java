package es.in2.issuer.infrastructure.config.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RemoteSignaturePropertiesTest {
    @Test
    void testRemoteSignatureProperties() {
        RemoteSignatureProperties.Paths paths = new RemoteSignatureProperties.Paths("signPath");
        RemoteSignatureProperties remoteSignatureProperties = new RemoteSignatureProperties("externalDomain", "internalDomain", paths);

        assertEquals("externalDomain", remoteSignatureProperties.externalDomain());
        assertEquals("internalDomain", remoteSignatureProperties.internalDomain());
        assertEquals(paths, remoteSignatureProperties.paths());
    }
}