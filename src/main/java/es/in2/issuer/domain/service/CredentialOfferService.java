package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.CustomCredentialOffer;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;

public interface CredentialOfferService {

    Mono<CustomCredentialOffer> buildCustomCredentialOffer(String credentialType, String preAuthCode);
    Mono<String> createCredentialOfferUri(String nonce);

//    Mono<CredentialOffer> buildCredentialOffer() throws MalformedURLException;
//    Mono<String> createCredentialOfferUriForPreAuthorizedCodeFlow(String accessToken, String credentialType);
//    Mono<CustomCredentialOffer> getCredentialOffer(String id);
}
