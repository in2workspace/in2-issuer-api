package es.in2.issuer.infrastructure.config;

import es.in2.issuer.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.infrastructure.config.properties.RemoteSignatureProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoteSignatureConfigTest {

    @Mock
    private ConfigAdapterFactory configAdapterFactory;

    @Mock
    private ConfigAdapter configAdapter;

    @Mock
    private RemoteSignatureProperties remoteSignatureProperties;

    private RemoteSignatureConfig remoteSignatureConfig;

    @BeforeEach
    void setUp() {
        when(configAdapterFactory.getAdapter()).thenReturn(configAdapter);
        remoteSignatureConfig = new RemoteSignatureConfig(configAdapterFactory, remoteSignatureProperties);
    }

    @Test
    void testGetRemoteSignatureExternalDomain() {
        // Arrange
        String expectedDomain = "https://signature.example.com";
        when(remoteSignatureProperties.externalDomain()).thenReturn("remote.signature.external.domain");
        when(configAdapter.getConfiguration("remote.signature.external.domain")).thenReturn(expectedDomain);

        // Act
        String actualDomain = remoteSignatureConfig.getRemoteSignatureExternalDomain();

        // Assert
        assertEquals(expectedDomain, actualDomain);
    }

    @Test
    void testGetRemoteSignatureInternalDomain() {
        // Arrange
        String expectedDomain = "https://internal.signature.example.com";
        when(remoteSignatureProperties.internalDomain()).thenReturn("remote.signature.internal.domain");
        when(configAdapter.getConfiguration("remote.signature.internal.domain")).thenReturn(expectedDomain);

        // Act
        String actualDomain = remoteSignatureConfig.getRemoteSignatureInternalDomain();

        // Assert
        assertEquals(expectedDomain, actualDomain);
    }

    @Test
    void testGetRemoteSignatureSignPath() {
        // Arrange
        String expectedPath = "/api/sign";
        RemoteSignatureProperties.Paths paths = mock(RemoteSignatureProperties.Paths.class);
        when(remoteSignatureProperties.paths()).thenReturn(paths);
        when(paths.signPath()).thenReturn("remote.signature.sign.path");
        when(configAdapter.getConfiguration("remote.signature.sign.path")).thenReturn(expectedPath);

        // Act
        String actualPath = remoteSignatureConfig.getRemoteSignatureSignPath();

        // Assert
        assertEquals(expectedPath, actualPath);
    }
}