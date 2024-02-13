package es.in2.issuer.api.service.impl;

import es.in2.issuer.api.model.dto.AppNonceValidationResponseDTO;
import es.in2.issuer.api.repository.CacheStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class NonceManagementServiceImplTest {

    @Mock
    private CacheStore<String> cacheStore;

    @InjectMocks
    private NonceManagementServiceImpl nonceManagementService;

    @Test
    void saveAccessTokenAndNonce() {
        AppNonceValidationResponseDTO appNonceValidationResponseDTO = new AppNonceValidationResponseDTO("token");

        doNothing().when(cacheStore).add(anyString(), anyString());

        nonceManagementService.saveAccessTokenAndNonce(appNonceValidationResponseDTO).block();

        verify(cacheStore).add(anyString(), anyString());
    }
}
