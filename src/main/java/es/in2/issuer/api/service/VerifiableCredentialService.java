package es.in2.issuer.api.service;

import es.in2.issuer.api.model.dto.CredentialRequestDTO;
import es.in2.issuer.api.model.dto.VerifiableCredentialResponseDTO;
import reactor.core.publisher.Mono;

public interface VerifiableCredentialService {
    Mono<VerifiableCredentialResponseDTO> generateVerifiableCredentialResponse(String username, CredentialRequestDTO credentialRequestDTO, String token);
    Mono<String> getVerifiableCredential(String credentialId);
}
