package es.in2.issuer.backend.domain.service;

import es.in2.issuer.backend.domain.exception.CredentialAlreadyIssuedException;
import es.in2.issuer.backend.domain.model.entities.DeferredCredentialMetadata;
import es.in2.issuer.backend.domain.service.impl.DeferredCredentialMetadataServiceImpl;
import es.in2.issuer.shared.domain.util.Utils;
import es.in2.issuer.shared.infrastructure.repository.CacheStoreRepository;
import es.in2.issuer.backend.infrastructure.repository.DeferredCredentialMetadataRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeferredCredentialMetadataServiceImplTest {

    @Mock
    private DeferredCredentialMetadataRepository deferredCredentialMetadataRepository;

    @Mock
    private CacheStoreRepository<String> cacheStoreRepository;

    @InjectMocks
    private DeferredCredentialMetadataServiceImpl deferredCredentialMetadataService;

    @Test
    void testValidateTransactionCode_Success() {
        // Arrange
        String transactionCode = "transaction-code";
        when(cacheStoreRepository.get(transactionCode)).thenReturn(Mono.just(transactionCode));
        when(cacheStoreRepository.delete(transactionCode)).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(deferredCredentialMetadataService.validateTransactionCode(transactionCode))
                .verifyComplete();

        // Assert
        verify(cacheStoreRepository, times(1)).get(transactionCode);
        verify(cacheStoreRepository, times(1)).delete(transactionCode);
    }

    @Test
    void testUpdateAuthServerNonceByAuthServerNonce_Success() {
        // Arrange
        String accessToken = "access-token";
        String preAuthCode = "pre-auth-code";
        DeferredCredentialMetadata deferredCredentialMetadata = new DeferredCredentialMetadata();
        when(deferredCredentialMetadataRepository.findByAuthServerNonce(preAuthCode)).thenReturn(Mono.just(deferredCredentialMetadata));
        when(deferredCredentialMetadataRepository.save(deferredCredentialMetadata)).thenReturn(Mono.just(deferredCredentialMetadata));

        // Act
        StepVerifier.create(deferredCredentialMetadataService.updateAuthServerNonceByAuthServerNonce(accessToken, preAuthCode))
                .verifyComplete();

        // Assert
        verify(deferredCredentialMetadataRepository, times(1)).findByAuthServerNonce(preAuthCode);
        verify(deferredCredentialMetadataRepository, times(1)).save(deferredCredentialMetadata);
    }

    @Test
    void testCreateDeferredCredentialMetadata_Success() {
        // Arrange
        String procedureId = UUID.randomUUID().toString();
        String nonce = "nonce";
        String transactionCode = "transaction-code";

        try (MockedStatic<Utils> mockUtils = mockStatic(Utils.class)) {
            mockUtils.when(Utils::generateCustomNonce).thenReturn(Mono.just(nonce));
        }
        when(cacheStoreRepository.add(anyString(), anyString())).thenReturn(Mono.just(transactionCode));
        when(deferredCredentialMetadataRepository.save(any(DeferredCredentialMetadata.class))).thenReturn(Mono.just(new DeferredCredentialMetadata()));

        // Act
        StepVerifier.create(deferredCredentialMetadataService.createDeferredCredentialMetadata(procedureId, "A", null))
                .expectNext(transactionCode)
                .verifyComplete();

        // Assert
        verify(cacheStoreRepository, times(1)).add(anyString(), anyString());
        verify(deferredCredentialMetadataRepository, times(1)).save(any(DeferredCredentialMetadata.class));
    }

    @Test
    void testUpdateTransactionCodeInDeferredCredentialMetadata_Success() {
        // Arrange
        String procedureId = UUID.randomUUID().toString();
        String nonce = "nonce";
        DeferredCredentialMetadata deferredCredentialMetadata = new DeferredCredentialMetadata();
        when(deferredCredentialMetadataRepository.findByProcedureId(UUID.fromString(procedureId))).thenReturn(Mono.just(deferredCredentialMetadata));
        try (MockedStatic<Utils> mockUtils = mockStatic(Utils.class)) {
            mockUtils.when(Utils::generateCustomNonce).thenReturn(Mono.just(nonce));
        }
        when(cacheStoreRepository.add(anyString(), anyString())).thenReturn(Mono.just(nonce));
        when(deferredCredentialMetadataRepository.save(any(DeferredCredentialMetadata.class))).thenReturn(Mono.just(deferredCredentialMetadata));

        // Act
        StepVerifier.create(deferredCredentialMetadataService.updateTransactionCodeInDeferredCredentialMetadata(procedureId))
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();

        // Assert
        verify(deferredCredentialMetadataRepository, times(1)).findByProcedureId(UUID.fromString(procedureId));
        verify(cacheStoreRepository, times(1)).add(anyString(), anyString());
        verify(deferredCredentialMetadataRepository, times(1)).save(any(DeferredCredentialMetadata.class));
    }

    @Test
    void testGetProcedureIdByTransactionCode_Success() {
        // Arrange
        String transactionCode = "transaction-code";
        DeferredCredentialMetadata deferredCredentialMetadata = new DeferredCredentialMetadata();
        deferredCredentialMetadata.setProcedureId(UUID.randomUUID());
        when(deferredCredentialMetadataRepository.findByTransactionCode(transactionCode)).thenReturn(Mono.just(deferredCredentialMetadata));

        // Act
        StepVerifier.create(deferredCredentialMetadataService.getProcedureIdByTransactionCode(transactionCode))
                .expectNext(deferredCredentialMetadata.getProcedureId().toString())
                .verifyComplete();

        // Assert
        verify(deferredCredentialMetadataRepository, times(1)).findByTransactionCode(transactionCode);
    }
    @Test
    void getProcedureIdByTransactionCode_whenTransactionCodeDoesNotExist_throwsCredentialAlreadyIssuedException() {
        String transactionCode = "transaction-code";
        when(deferredCredentialMetadataRepository.findByTransactionCode(transactionCode)).thenReturn(Mono.empty());

        StepVerifier.create(deferredCredentialMetadataService.getProcedureIdByTransactionCode(transactionCode))
                .expectError(CredentialAlreadyIssuedException.class)
                .verify();

        verify(deferredCredentialMetadataRepository, times(1)).findByTransactionCode(transactionCode);
    }

    @Test
    void testGetProcedureIdByAuthServerNonce_Success() {
        // Arrange
        String authServerNonce = "auth-server-nonce";
        DeferredCredentialMetadata deferredCredentialMetadata = new DeferredCredentialMetadata();
        deferredCredentialMetadata.setProcedureId(UUID.randomUUID());
        when(deferredCredentialMetadataRepository.findByAuthServerNonce(authServerNonce)).thenReturn(Mono.just(deferredCredentialMetadata));

        // Act
        StepVerifier.create(deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce))
                .expectNext(deferredCredentialMetadata.getProcedureId().toString())
                .verifyComplete();

        // Assert
        verify(deferredCredentialMetadataRepository, times(1)).findByAuthServerNonce(authServerNonce);
    }

    @Test
    void testUpdateAuthServerNonceByTransactionCode_Success() {
        // Arrange
        String transactionCode = "transaction-code";
        String authServerNonce = "auth-server-nonce";
        DeferredCredentialMetadata deferredCredentialMetadata = new DeferredCredentialMetadata();
        when(deferredCredentialMetadataRepository.findByTransactionCode(transactionCode)).thenReturn(Mono.just(deferredCredentialMetadata));
        when(deferredCredentialMetadataRepository.save(deferredCredentialMetadata)).thenReturn(Mono.just(deferredCredentialMetadata));

        // Act
        StepVerifier.create(deferredCredentialMetadataService.updateAuthServerNonceByTransactionCode(transactionCode, authServerNonce))
                .verifyComplete();

        // Assert
        verify(deferredCredentialMetadataRepository, times(1)).findByTransactionCode(transactionCode);
        verify(deferredCredentialMetadataRepository, times(1)).save(deferredCredentialMetadata);
    }

    @Test
    void testUpdateDeferredCredentialMetadataByAuthServerNonce_Success() {
        // Arrange
        String authServerNonce = "auth-server-nonce";
        String format = "format";
        DeferredCredentialMetadata deferredCredentialMetadata = new DeferredCredentialMetadata();
        when(deferredCredentialMetadataRepository.findByAuthServerNonce(authServerNonce)).thenReturn(Mono.just(deferredCredentialMetadata));
        when(deferredCredentialMetadataRepository.save(deferredCredentialMetadata)).thenReturn(Mono.just(deferredCredentialMetadata));

        // Act
        StepVerifier.create(deferredCredentialMetadataService.updateDeferredCredentialMetadataByAuthServerNonce(authServerNonce, format))
                .expectNextMatches(Objects::nonNull)
                .verifyComplete();

        // Assert
        verify(deferredCredentialMetadataRepository, times(1)).findByAuthServerNonce(authServerNonce);
        verify(deferredCredentialMetadataRepository, times(1)).save(deferredCredentialMetadata);
    }

    @Test
    void testUpdateVcByProcedureId_Success() {
        // Arrange
        String vc = "jwtCredential";
        String procedureId = UUID.randomUUID().toString();
        DeferredCredentialMetadata deferredCredentialMetadata = new DeferredCredentialMetadata();
        when(deferredCredentialMetadataRepository.findByProcedureId(UUID.fromString(procedureId))).thenReturn(Mono.just(deferredCredentialMetadata));
        when(deferredCredentialMetadataRepository.save(deferredCredentialMetadata)).thenReturn(Mono.just(deferredCredentialMetadata));

        // Act
        StepVerifier.create(deferredCredentialMetadataService.updateVcByProcedureId(vc, procedureId))
                .verifyComplete();

        // Assert
        verify(deferredCredentialMetadataRepository, times(1)).findByProcedureId(UUID.fromString(procedureId));
        verify(deferredCredentialMetadataRepository, times(1)).save(deferredCredentialMetadata);
    }

    @Test
    void testGetVcByTransactionId_Success() {
        // Arrange
        String transactionId = UUID.randomUUID().toString();
        DeferredCredentialMetadata deferredCredentialMetadata = new DeferredCredentialMetadata();
        deferredCredentialMetadata.setVc("jwtCredential");
        deferredCredentialMetadata.setId(UUID.randomUUID());
        deferredCredentialMetadata.setProcedureId(UUID.randomUUID());
        when(deferredCredentialMetadataRepository.findByTransactionId(transactionId)).thenReturn(Mono.just(deferredCredentialMetadata));

        // Act
        StepVerifier.create(deferredCredentialMetadataService.getVcByTransactionId(transactionId))
                .expectNextMatches(response -> response.vc().equals(deferredCredentialMetadata.getVc())
                        && response.id().equals(deferredCredentialMetadata.getId().toString())
                        && response.procedureId().equals(deferredCredentialMetadata.getProcedureId().toString()))
                .verifyComplete();

        // Assert
        verify(deferredCredentialMetadataRepository, times(1)).findByTransactionId(transactionId);
    }

    @Test
    void testDeleteDeferredCredentialMetadataById_Success() {
        // Arrange
        String id = UUID.randomUUID().toString();
        when(deferredCredentialMetadataRepository.deleteById(UUID.fromString(id))).thenReturn(Mono.empty());

        // Act
        StepVerifier.create(deferredCredentialMetadataService.deleteDeferredCredentialMetadataById(id))
                .verifyComplete();

        // Assert
        verify(deferredCredentialMetadataRepository, times(1)).deleteById(UUID.fromString(id));
    }

    @Test
    void testGetOperationModeByAuthServerNonce_Success() {
        // Arrange
        String authServerNonce = "auth-server-nonce";
        DeferredCredentialMetadata deferredCredentialMetadata = new DeferredCredentialMetadata();
        deferredCredentialMetadata.setProcedureId(UUID.randomUUID());
        deferredCredentialMetadata.setOperationMode("A");
        when(deferredCredentialMetadataRepository.findByAuthServerNonce(authServerNonce)).thenReturn(Mono.just(deferredCredentialMetadata));

        // Act
        StepVerifier.create(deferredCredentialMetadataService.getOperationModeByAuthServerNonce(authServerNonce))
                .expectNext(deferredCredentialMetadata.getOperationMode())
                .verifyComplete();

        // Assert
        verify(deferredCredentialMetadataRepository, times(1)).findByAuthServerNonce(authServerNonce);
    }

    @Test
    void testGetOperationModeByProcedureId_Success() {
        // Arrange
        UUID procedureId = UUID.randomUUID();
        DeferredCredentialMetadata deferredCredentialMetadata = new DeferredCredentialMetadata();
        deferredCredentialMetadata.setProcedureId(procedureId);
        deferredCredentialMetadata.setOperationMode("A");
        when(deferredCredentialMetadataRepository.findByProcedureId(procedureId)).thenReturn(Mono.just(deferredCredentialMetadata));

        // Act
        StepVerifier.create(deferredCredentialMetadataService.getOperationModeByProcedureId(String.valueOf(procedureId)))
                .expectNext(deferredCredentialMetadata.getOperationMode())
                .verifyComplete();

        // Assert
        verify(deferredCredentialMetadataRepository, times(1)).findByProcedureId(procedureId);
    }

    @Test
    void validateCTransactionCode_whenCTransactionCodeExists_returnsTransactionCode() {
        String cTransactionCode = "c-transaction-code";
        String transactionCode = "transaction-code";
        when(cacheStoreRepository.get(cTransactionCode)).thenReturn(Mono.just(transactionCode));
        when(cacheStoreRepository.delete(cTransactionCode)).thenReturn(Mono.empty());

        StepVerifier.create(deferredCredentialMetadataService.validateCTransactionCode(cTransactionCode))
                .expectNext(transactionCode)
                .verifyComplete();

        verify(cacheStoreRepository, times(1)).get(cTransactionCode);
        verify(cacheStoreRepository, times(1)).delete(cTransactionCode);
    }

    @Test
    void validateCTransactionCode_whenCTransactionCodeDoesNotExist_returnsEmpty() {
        String cTransactionCode = "c-transaction-code";
        when(cacheStoreRepository.get(cTransactionCode)).thenReturn(Mono.empty());

        StepVerifier.create(deferredCredentialMetadataService.validateCTransactionCode(cTransactionCode))
                .verifyComplete();

        verify(cacheStoreRepository, times(1)).get(cTransactionCode);
        verify(cacheStoreRepository, never()).delete(cTransactionCode);
    }

    @Test
    void validateCTransactionCode_whenDeleteFails_throwsException() {
        String cTransactionCode = "c-transaction-code";
        String transactionCode = "transaction-code";
        when(cacheStoreRepository.get(cTransactionCode)).thenReturn(Mono.just(transactionCode));
        when(cacheStoreRepository.delete(cTransactionCode)).thenReturn(Mono.error(new RuntimeException("Delete failed")));

        StepVerifier.create(deferredCredentialMetadataService.validateCTransactionCode(cTransactionCode))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Delete failed"))
                .verify();

        verify(cacheStoreRepository, times(1)).get(cTransactionCode);
        verify(cacheStoreRepository, times(1)).delete(cTransactionCode);
    }

    @Test
    void updatecacheStore_whenNonceGeneratedAndAddedToCache_returnsMapWithCTransactionCodeDetails() {
        String transactionCode = "transaction-code";
        String cTransactionCode = "c-transaction-code";
        int expiry = 3600;

        try (MockedStatic<Utils> mockUtils = mockStatic(Utils.class)) {
            mockUtils.when(Utils::generateCustomNonce).thenReturn(Mono.just(cTransactionCode));
            when(cacheStoreRepository.add(cTransactionCode, transactionCode)).thenReturn(Mono.empty());
            when(cacheStoreRepository.getCacheExpiryInSeconds()).thenReturn(Mono.just(expiry));

            StepVerifier.create(deferredCredentialMetadataService.updateCacheStoreForCTransactionCode(transactionCode))
                    .expectNextMatches(map ->
                            cTransactionCode.equals(map.get("cTransactionCode")) &&
                                    expiry == (int) map.get("cTransactionCodeExpiresIn")
                    )
                    .verifyComplete();
        }

        verify(cacheStoreRepository, times(1)).add(cTransactionCode, transactionCode);
        verify(cacheStoreRepository, times(1)).getCacheExpiryInSeconds();
    }


}