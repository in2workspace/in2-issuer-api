package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.AppNonceValidationResponse;
import es.in2.issuer.domain.service.impl.NonceManagementServiceImpl;
import es.in2.issuer.infrastructure.repository.CacheStore;
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
        AppNonceValidationResponse appNonceValidationResponse = new AppNonceValidationResponse("token");

        doNothing().when(cacheStore).add(anyString(), anyString());

        nonceManagementService.saveAccessTokenAndNonce(appNonceValidationResponse).block();

        verify(cacheStore).add(anyString(), anyString());
    }
}
