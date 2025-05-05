package es.in2.issuer.backend.backoffice.domain.service;

import es.in2.issuer.backend.shared.domain.model.dto.CredentialOfferData;
import es.in2.issuer.backend.shared.domain.model.dto.Grants;
import reactor.core.publisher.Mono;

public interface CredentialOfferService {
    Mono<CredentialOfferData> buildCustomCredentialOffer(String credentialType, Grants grants, String employeeEmail, String pin);
    Mono<String> createCredentialOfferUriResponse(String nonce);
}
