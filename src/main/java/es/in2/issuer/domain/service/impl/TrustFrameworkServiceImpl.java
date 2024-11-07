package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.exception.TrustFrameworkDidException;
import es.in2.issuer.domain.exception.TrustFrameworkException;
import es.in2.issuer.domain.model.dto.ParticipantDidRequest;
import es.in2.issuer.domain.service.TrustFrameworkService;
import es.in2.issuer.infrastructure.config.TrustFrameworkConfig;
import es.in2.issuer.infrastructure.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static es.in2.issuer.domain.util.EndpointsConstants.TRUST_FRAMEWORK_ISSUER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrustFrameworkServiceImpl implements TrustFrameworkService {

    private final WebClientConfig webClient;
    private final TrustFrameworkConfig trustFrameworkConfig;

    @Override
    public Mono<Void> registerDid(String processId, String did) {
        ParticipantDidRequest request = ParticipantDidRequest.builder().did(did).build();
        return webClient.commonWebClient()
                .post()
                .uri(trustFrameworkConfig.getTrustFrameworkUrl() + TRUST_FRAMEWORK_ISSUER)
                .body(Mono.just(request), ParticipantDidRequest.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().value() == 409) {
                        log.error("Did already exists in the trusted participant list");
                        return Mono.error(new TrustFrameworkDidException("Did already exists in the trusted participant list"));
                    } else if (response.statusCode().value() == 201) {
                        log.info("Successfully registered the did");
                        return Mono.empty();
                    } else {
                        log.error("ProcessId: {} TrustFrameworkServiceImpl -- registerDid() -- Unexpected error with status code: {} , error: {}", processId, response.statusCode(), response);
                        return Mono.error(new TrustFrameworkException("Unexpected error in TrustFramework"));
                    }
                });
    }

    @Override
    public Mono<Boolean> validateDidFormat(String processId, String did) {
        if (did == null || did.trim().isEmpty()) {
            log.error("ProcessId: {} TrustFrameworkServiceImpl -- validateDid() -- DID is null or blank", processId);
            return Mono.just(false);
        }
        String pattern = "^did:[^:]*:[^:]*$";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(did);

        boolean isValid = matcher.matches();
        if (!isValid) {
            log.error("ProcessId: {} TrustFrameworkServiceImpl -- validateDid() -- Invalid did format", processId);
            return Mono.just(false);
        }
        return Mono.just(true);
    }

}
