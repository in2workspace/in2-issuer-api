package es.in2.issuer.domain.service;

import reactor.core.publisher.Mono;

public interface MarketplaceService {
    Mono<Void> sendVerifiableCertificationToMarketplace(String verifiableCertification);
}
