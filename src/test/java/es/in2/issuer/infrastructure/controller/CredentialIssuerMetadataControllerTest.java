package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.model.dto.CredentialIssuerMetadata;
import es.in2.issuer.domain.service.CredentialIssuerMetadataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialIssuerMetadataControllerTest {

    @Mock
    private CredentialIssuerMetadataService credentialIssuerMetadataService;

    @InjectMocks
    private CredentialIssuerMetadataController controller;

    @Test
    void testGetCredentialIssuer_Metadata_Success() {
        // Arrange
        CredentialIssuerMetadata mockedMetadata = new CredentialIssuerMetadata("", "", "", "", null);
        when(credentialIssuerMetadataService.generateOpenIdCredentialIssuer()).thenReturn(Mono.just(mockedMetadata));
        // Mock
        ServerWebExchange mockExchange = mock(ServerWebExchange.class);
        ServerHttpResponse mockResponse = mock(ServerHttpResponse.class);
        when(mockExchange.getResponse()).thenReturn(mockResponse);
        HttpHeaders mockHeaders = new HttpHeaders();
        when(mockResponse.getHeaders()).thenReturn(mockHeaders);
        // Act
        Mono<CredentialIssuerMetadata> result = controller.getCredentialIssuerMetadata(mockExchange);
        // Assert
        result.subscribe(issuerData -> {
            // Check the mocked response
            assert issuerData.equals("MockedIssuerData");
        });
        // Verify service method was called
        verify(credentialIssuerMetadataService, times(1)).generateOpenIdCredentialIssuer();
    }

}
