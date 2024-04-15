package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.CredentialIssuerMetadata;
import es.in2.issuer.domain.service.impl.CredentialIssuerMetadataServiceImpl;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.iam.service.GenericIamAdapter;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


@ExtendWith(MockitoExtension.class)
class CredentialIssuerMetadataServiceImplTest {

    @Mock
    private IamAdapterFactory iamAdapterFactory;

    @Mock
    private AppConfiguration appConfiguration;

    @InjectMocks
    private CredentialIssuerMetadataServiceImpl service;

    @BeforeEach
    void setUp() {
        // Setup mocks
        when(appConfiguration.getIssuerExternalDomain()).thenReturn("https://example.com");
        GenericIamAdapter adapter = mock(GenericIamAdapter.class);
        when(iamAdapterFactory.getAdapter()).thenReturn(adapter);
        when(adapter.getTokenUri()).thenReturn("https://iam.example.com/token");
    }

    @Test
    void testGenerateOpenIdCredentialIssuer() {
        // Execute the service method
        Mono<CredentialIssuerMetadata> result = service.generateOpenIdCredentialIssuer();

        // Verify results
        StepVerifier.create(result)
                .assertNext(metadata -> {
                    assertEquals("https://example.com", metadata.credentialIssuer());
                    assertEquals("https://example.com/api/vc/credential", metadata.credentialEndpoint());
                    assertEquals("https://example.com/api/vc/batch_credential", metadata.batchCredentialEndpoint());
                    assertEquals("https://iam.example.com/token", metadata.credentialToken());
                    assertNotNull(metadata.credentialConfigurationsSupported());
                })
                .verifyComplete();

        // Verify interactions
        verify(appConfiguration).getIssuerExternalDomain();
        verify(iamAdapterFactory).getAdapter();
    }
}
