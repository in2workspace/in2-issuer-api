package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.exception.TrustFrameworkException;
import es.in2.issuer.domain.model.dto.ParticipantDidRequest;
import es.in2.issuer.domain.service.TrustFrameworkService;
import es.in2.issuer.infrastructure.config.TrustFrameworkConfig;
import es.in2.issuer.infrastructure.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static es.in2.issuer.domain.util.EndpointsConstants.TRUST_FRAMEWORK_ISSUER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrustFrameworkServiceImpl implements TrustFrameworkService {

    private final WebClientConfig webClient;
    private final TrustFrameworkConfig trustFrameworkConfig;

    @Override
    public Mono<Void> registerParticipant(String did) {
        ParticipantDidRequest request = ParticipantDidRequest.builder().did(did).build();
        return webClient.commonWebClient()
                .post()
                .uri(trustFrameworkConfig.getTrustFrameworkUrl() + TRUST_FRAMEWORK_ISSUER)
                .body(Mono.just(request), ParticipantDidRequest.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().value() == 409) {
                        return Mono.error(new TrustFrameworkException("DID already exists in the trusted list"));
                    } else if (response.statusCode().is5xxServerError()) {
                        return Mono.error(new TrustFrameworkException("Server error occurred: " + response.statusCode()));
                    } else if (response.statusCode().value() == 201) {
                        return Mono.empty();
                    } else {
                        return Mono.error(new TrustFrameworkException("Unexpected response status: " + response.statusCode()));
                    }
                });
    }
}
