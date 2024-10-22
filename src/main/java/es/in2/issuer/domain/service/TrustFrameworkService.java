package es.in2.issuer.domain.service;

import reactor.core.publisher.Mono;

public interface TrustFrameworkService {
    Mono<Void> registerParticipant(String did);
}
