package es.in2.issuer.api.controller;

import es.in2.issuer.api.model.dto.CredentialIssuerMetadata;
import es.in2.issuer.api.service.CredentialIssuerMetadataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

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
        Mono<String> result = controller.getOpenIdCredentialIssuer();

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
        when(credentialIssuerMetadataService.generateOpenIdCredentialIssuer()).thenThrow(new RuntimeException("Mocked Exception"));

        // Act
        Mono<String> result = controller.getOpenIdCredentialIssuer();

        // Assert
        result.subscribe(
                issuerData -> {
                    // Should not reach here
                    assert false;
                },
                throwable -> {
                    // Check the exception
                    assert throwable instanceof RuntimeException;
                    assert throwable.getMessage().equals("Mocked Exception");
                }
        );

        // Verify service method was called
        verify(credentialIssuerMetadataService, times(1)).generateOpenIdCredentialIssuer();
    }
}
