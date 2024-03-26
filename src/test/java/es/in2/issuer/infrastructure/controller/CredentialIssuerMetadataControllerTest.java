package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.model.CredentialIssuerMetadata;
import es.in2.issuer.domain.service.CredentialIssuerMetadataService;
import es.in2.issuer.infrastructure.controller.CredentialIssuerMetadataController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialIssuerMetadataControllerTest {

    @Mock
    private CredentialIssuerMetadataService credentialIssuerMetadataService;

    @InjectMocks
    private CredentialIssuerMetadataController controller;

    @Test
    void testGetOpenIdCredentialIssuer_Success() {
        // Arrange
        CredentialIssuerMetadata mockedMetadata = new CredentialIssuerMetadata("","","",null); // You may need to create an instance with relevant data
        when(credentialIssuerMetadataService.generateOpenIdCredentialIssuer()).thenReturn(Mono.just(mockedMetadata));

        // Act
        Mono<CredentialIssuerMetadata> result = controller.getOpenIdCredentialIssuer();

        // Assert
        result.subscribe(issuerData -> {
            // Check the mocked response
            assert issuerData.equals("MockedIssuerData");
        });

        // Verify service method was called
        verify(credentialIssuerMetadataService, times(1)).generateOpenIdCredentialIssuer();
    }

    @Test
    void testGetOpenIdCredentialIssuer_Exception() {
        // Arrange
        when(credentialIssuerMetadataService.generateOpenIdCredentialIssuer()).thenReturn(Mono.error(new RuntimeException("Mocked Exception")));

        // Act & Assert
        StepVerifier.create(controller.getOpenIdCredentialIssuer())
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getCause() instanceof RuntimeException &&
                                "Mocked Exception".equals(throwable.getCause().getMessage()))
                .verify();

        // Verify service method was called
        verify(credentialIssuerMetadataService, times(1)).generateOpenIdCredentialIssuer();
    }
}
