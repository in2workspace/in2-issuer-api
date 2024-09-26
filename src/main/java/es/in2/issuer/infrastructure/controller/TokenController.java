package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import es.in2.issuer.domain.service.AccessTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/**
 * @deprecated This class is obsolete and will be removed in future versions.
 * @since 1.0.0
 */
@Deprecated(since = "1.0.0", forRemoval = true)
@Slf4j
@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final AccessTokenService accessTokenService;

    @PostMapping("/m2m")
    public Mono<VerifierOauth2AccessToken> getToken() {
        return accessTokenService.getM2MToken();
    }

    @PostMapping
    public Mono<Object> handleData(ServerWebExchange exchange) {
        Mono<MultiValueMap<String, String>> formDataMono = exchange.getFormData();

        log.info("Get formDataMono. [Exchange:{}]", exchange);

        return formDataMono.flatMap(formData -> {
            log.info("\n==================================\n");
            log.info("[TokenFormData:{}]", formData);
            log.info("\n==================================\n");

            var client = WebClient.builder()
                    .baseUrl("http://dome-issuer-keycloak:8080")
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                    .build();

            return client.post()
                    .uri("issuer-keycloak/realms/CredentialIssuer/verifiable-credential/did:key:z6MkqmaCT2JqdUtLeKah7tEVfNXtDXtQyj4yxEgV11Y5CqUa/token")
                    .body(BodyInserters.fromFormData("grant_type", Objects.requireNonNull(formData.getFirst("grant_type")))
                            .with("pre-authorized_code", Objects.requireNonNull(formData.getFirst("pre-authorized_code")))
                            .with("tx_code", Objects.requireNonNull(formData.getFirst("tx_code"))))
                    .retrieve()
                    .bodyToMono(Object.class)
                    .onErrorResume(error -> {
                        log.info("\n==================================\n");
                        log.error("[WebClientException:{}]", error.getMessage());
                        log.info("\n==================================\n");

                        return Mono.error(error);
                    });
        });
    }
}
