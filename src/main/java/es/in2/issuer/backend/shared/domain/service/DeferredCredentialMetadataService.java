package es.in2.issuer.backend.shared.domain.service;

import es.in2.issuer.backend.shared.domain.model.dto.DeferredCredentialMetadataDeferredResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface DeferredCredentialMetadataService {
    Mono<Map<String, Object>> updateCacheStoreForCTransactionCode(String transactionCode);
    Mono<String> validateCTransactionCode(String cTransactionCode);
    Mono<String> updateTransactionCodeInDeferredCredentialMetadata(String procedureId);
    Mono<String> getProcedureIdByTransactionCode(String transactionCode);
    Mono<String> getProcedureIdByAuthServerNonce(String authServerNonce);
    Mono<String> getOperationModeByAuthServerNonce(String authServerNonce);
    Mono<String> getOperationModeByProcedureId(String procedureId);
    Mono<Void> updateAuthServerNonceByTransactionCode(String transactionCode, String authServerNonce);
    Mono<String> updateDeferredCredentialMetadataByAuthServerNonce(String authServerNonce, String format);
    Mono<Void> updateDeferredCredentialByAuthServerNonce(String authServerNonce, String format);
    Mono<Void> validateTransactionCode(String transactionCode);
    Mono<Void> updateAuthServerNonceByAuthServerNonce(String accessToken, String preAuthCode);
    Mono<Void> updateVcByProcedureId(String vc, String procedureId);
    Mono<DeferredCredentialMetadataDeferredResponse> getVcByTransactionId(String transactionId);
    Mono<Void> deleteDeferredCredentialMetadataById(String id);
    Mono<Void> deleteDeferredCredentialMetadataByAuthServerNonce(String authServerNonce);
}
