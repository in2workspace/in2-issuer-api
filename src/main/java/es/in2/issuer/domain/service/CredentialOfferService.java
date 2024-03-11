package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.CredentialOfferForPreAuthorizedCodeFlow;
import reactor.core.publisher.Mono;

public interface CredentialOfferService {
    Mono<String> createCredentialOfferUriForPreAuthorizedCodeFlow(String accessToken, String credentialType);
    Mono<CredentialOfferForPreAuthorizedCodeFlow> getCredentialOffer(String id);
}
