package es.in2.issuer.application.service;

import es.in2.issuer.domain.model.CustomCredentialOffer;
import eu.europa.ec.eudi.openid4vci.CredentialOffer;
import reactor.core.publisher.Mono;

public interface CredentialOfferIssuanceService {
    Mono<String> buildCredentialOfferUri(String credentialType);
    Mono<CustomCredentialOffer> getCustomCredentialOffer(String nonce);
}
