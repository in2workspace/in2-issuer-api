package es.in2.issuer.backend.domain.service;

import es.in2.issuer.backend.domain.model.dto.CredentialOfferData;
import es.in2.issuer.shared.domain.model.dto.Grant;
import reactor.core.publisher.Mono;

public interface CredentialOfferService {
    Mono<CredentialOfferData> buildCustomCredentialOffer(String credentialType, Grant grant, String employeeEmail, String pin);
    Mono<String> createCredentialOfferUriResponse(String nonce);
}
