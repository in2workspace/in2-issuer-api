package es.in2.issuer.infrastructure.config.properties;

import es.in2.issuer.backend.infrastructure.config.properties.RemoteSignatureProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RemoteSignaturePropertiesTest {
    @Test
    void testRemoteSignatureProperties() {
        RemoteSignatureProperties.Paths paths = new RemoteSignatureProperties.Paths("signPath");
        RemoteSignatureProperties remoteSignatureProperties = new RemoteSignatureProperties("domain", paths);

        assertEquals("domain", remoteSignatureProperties.domain());
        assertEquals(paths, remoteSignatureProperties.paths());
    }
}