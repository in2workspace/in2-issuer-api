package es.in2.issuer.domain.service;

import reactor.core.publisher.Mono;

public interface DeferredCredentialMetadataService {
    Mono<String> createDeferredCredentialMetadata(String procedureId);
    Mono<String> getProcedureIdByTransactionCode(String transactionCode);
    Mono<Void> updateAuthServerNonceByTransactionCode(String transactionCode, String authServerNonce);
    Mono<Void> validateTransactionCode(String transactionCode);
}
