package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.dto.DeferredCredentialMetadataDeferredResponse;
import reactor.core.publisher.Mono;

public interface DeferredCredentialMetadataService {
    Mono<String> createDeferredCredentialMetadata(String procedureId);
    Mono<String> getProcedureIdByTransactionCode(String transactionCode);
    Mono<String> getProcedureIdByAuthServerNonce(String authServerNonce);
    Mono<Void> updateAuthServerNonceByTransactionCode(String transactionCode, String authServerNonce);
    Mono<String> updateDeferredCredentialMetadataByAuthServerNonce(String authServerNonce, String format);
    Mono<Void> validateTransactionCode(String transactionCode);
    Mono<Void> updateAuthServerNonceByAuthServerNonce(String accessToken, String preAuthCode);
    Mono<Void> updateVcByProcedureId(String vc, String procedureId);
    Mono<DeferredCredentialMetadataDeferredResponse> getVcByTransactionId(String transactionId);
    Mono<Void> deleteDeferredCredentialMetadataById(String id);
}
