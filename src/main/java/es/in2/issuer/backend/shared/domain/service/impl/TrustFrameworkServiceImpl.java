package es.in2.issuer.backend.shared.domain.service.impl;

import es.in2.issuer.backend.shared.domain.model.dto.ParticipantDidRequest;
import es.in2.issuer.backend.shared.domain.service.TrustFrameworkService;
import es.in2.issuer.backend.shared.infrastructure.config.AppConfig;
import es.in2.issuer.backend.shared.infrastructure.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static es.in2.issuer.backend.shared.domain.util.EndpointsConstants.TRUST_FRAMEWORK_ISSUER;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrustFrameworkServiceImpl implements TrustFrameworkService {

    private final WebClientConfig webClient;
    private final AppConfig appConfig;

    @Override
    public Mono<Void> registerDid(String processId, String did) {
        ParticipantDidRequest request = ParticipantDidRequest.builder().did(did).build();
        return webClient.commonWebClient()
                .post()
                .uri(appConfig.getTrustFrameworkUrl() + TRUST_FRAMEWORK_ISSUER)
                .body(Mono.just(request), ParticipantDidRequest.class)
                .exchangeToMono(response -> {
                    if (response.statusCode().value() == 409) {
                        log.error("ProcessId: {} TrustFrameworkServiceImpl -- registerDid() -- Did {} already exists in the trusted participant list", processId, did);
                        return Mono.empty();
                    } else if (response.statusCode().value() == 201) {
                        log.info("ProcessId: {} TrustFrameworkServiceImpl -- registerDid() -- Successfully registered the did {}", processId, did);
                        return Mono.empty();
                    } else {
                        log.error("ProcessId: {} TrustFrameworkServiceImpl -- registerDid() -- Unexpected error with status code: {} , error: {}", processId, response.statusCode(), response);
                        return Mono.empty();
                    }
                });
    }

    @Override
    public Mono<Boolean> validateDidFormat(String processId, String did) {
        if (did == null || did.trim().isEmpty()) {
            log.error("ProcessId: {} TrustFrameworkServiceImpl -- validateDidFormat() -- DID is null or blank", processId);
            return Mono.just(false);
        }
        String pattern = "^did:[^:]*:[^:]*$";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(did);

        boolean isValid = matcher.matches();
        if (!isValid) {
            log.error("ProcessId: {} TrustFrameworkServiceImpl -- validateDidFormat() -- Invalid did format", processId);
            return Mono.just(false);
        }
        return Mono.just(true);
    }

}
