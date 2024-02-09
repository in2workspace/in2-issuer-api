package es.in2.issuer.api.service;

import es.in2.issuer.api.model.dto.AppNonceValidationResponseDTO;
import es.in2.issuer.api.model.dto.NonceResponseDTO;
import reactor.core.publisher.Mono;

public interface NonceManagementService {
    Mono<NonceResponseDTO> saveAccessTokenAndNonce(AppNonceValidationResponseDTO appNonceValidationResponseDTO);
}
