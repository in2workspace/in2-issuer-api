package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.model.AppNonceValidationResponse;
import es.in2.issuer.domain.model.NonceResponse;
import es.in2.issuer.domain.service.NonceManagementService;
import es.in2.issuer.infrastructure.controller.NonceManagementController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NonceManagementControllerTest {

    @Mock
    private NonceManagementService nonceManagementService;

    @InjectMocks
    private NonceManagementController controller;

    @Test
    void testSaveAccessTokenAndNonce_Success() {
        // Arrange
        AppNonceValidationResponse requestDTO = new AppNonceValidationResponse("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        NonceResponse nonceResponse = new NonceResponse("", "");
        when(nonceManagementService.saveAccessTokenAndNonce(requestDTO)).thenReturn(Mono.just(nonceResponse));

        Mono<NonceResponse> result = controller.saveAccessTokenAndNonce(requestDTO);

        // Assert
        result.subscribe(issuerData -> {
            // Check the mocked response
            assert issuerData.equals(nonceResponse);
        });

        // Verify service method was called
        verify(nonceManagementService, times(1)).saveAccessTokenAndNonce(requestDTO);
    }

}
