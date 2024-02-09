package es.in2.issuer.waltid.ssikit;

import reactor.core.publisher.Mono;

public interface CustomDidService {

    Mono<String> generateDidKey();

    Mono<String> generateDidKeyWithKid(String kid);
}