package es.in2.issuer.api.service;

import es.in2.issuer.api.model.dto.CredentialOfferForPreAuthorizedCodeFlow;
import reactor.core.publisher.Mono;

public interface CredentialOfferService {
    Mono<String> createCredentialOfferUriForPreAuthorizedCodeFlow(String accessToken, String credentialType);
    Mono<CredentialOfferForPreAuthorizedCodeFlow> getCredentialOffer(String id);
}
