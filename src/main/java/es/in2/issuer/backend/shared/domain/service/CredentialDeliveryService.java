package es.in2.issuer.backend.shared.domain.service;

import reactor.core.publisher.Mono;

public interface CredentialDeliveryService {
    Mono<Void> sendVcToResponseUri(String responseUri, String encodedVc, String accessToken, String productId, String companyEmail);
}
