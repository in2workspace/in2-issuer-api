package es.in2.issuer.api.controller;

import es.in2.issuer.api.model.dto.AppNonceValidationResponseDTO;
import es.in2.issuer.api.model.dto.NonceResponseDTO;
import es.in2.issuer.api.service.NonceManagementService;
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
        AppNonceValidationResponseDTO requestDTO = new AppNonceValidationResponseDTO("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
        NonceResponseDTO nonceResponseDTO = new NonceResponseDTO("","");
        when(nonceManagementService.saveAccessTokenAndNonce(requestDTO)).thenReturn(Mono.just(nonceResponseDTO));

        Mono<NonceResponseDTO> result = controller.saveAccessTokenAndNonce(requestDTO);

        // Assert
        result.subscribe(issuerData -> {
            // Check the mocked response
            assert issuerData.equals(nonceResponseDTO);
        });

        // Verify service method was called
        verify(nonceManagementService, times(1)).saveAccessTokenAndNonce(requestDTO);
    }

}
