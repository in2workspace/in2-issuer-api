package es.in2.issuer.domain.service;

import es.in2.issuer.domain.service.impl.CredentialIssuerMetadataServiceImpl;
import es.in2.issuer.domain.util.HttpUtils;
import es.in2.issuer.infrastructure.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import static es.in2.issuer.domain.util.HttpUtils.ensureUrlHasProtocol;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialIssuerMetadataServiceImplTest {

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private CredentialIssuerMetadataServiceImpl service;

    @Test
    void testGenerateOpenIdCredentialIssuer() {
        try (MockedStatic<HttpUtils> ignored = Mockito.mockStatic(HttpUtils.class)) {
            String issuerUrl = "https://example.com";

            when(appConfig.getIssuerApiExternalDomain()).thenReturn(issuerUrl);
            when(ensureUrlHasProtocol(issuerUrl)).thenReturn(issuerUrl);
            // Verify results
            StepVerifier.create(service.generateOpenIdCredentialIssuer())
                    .assertNext(metadata -> {
                        assertEquals("https://example.com", metadata.credentialIssuer());
                        assertEquals("https://example.com/api/v1/vc/credential", metadata.credentialEndpoint());
                        assertEquals("https://example.com/api/v1/vc/batch_credential", metadata.batchCredentialEndpoint());
                        assertNotNull(metadata.credentialConfigurationsSupported());
                    })
                    .verifyComplete();

            // Verify interactions
            verify(appConfig).getIssuerApiExternalDomain();
        }
    }

}
