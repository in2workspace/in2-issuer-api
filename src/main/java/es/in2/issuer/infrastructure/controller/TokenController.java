package es.in2.issuer.infrastructure.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.net.URI;
import java.time.Duration;

@Slf4j
@RestController
@RequestMapping("/token")
public class TokenController {

    private static final ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
            .maxConnections(500)
            .maxIdleTime(Duration.ofSeconds(50))
            .maxLifeTime(Duration.ofSeconds(300))
            .evictInBackground(Duration.ofSeconds(80))
            .build();

    @PostMapping(value = "/token", consumes = "application/x-www-form-urlencoded")
    public Mono<Object> tokenBypass(@RequestBody LinkedMultiValueMap<String, String> formData) {
        log.debug("Start token redirection: [formData:{}]", formData);
        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create(connectionProvider).followRedirect(false))
                )
                .build();

        return webClient.post()
                .uri(URI.create("https://in2-dome-marketplace-test.org/issuer-keycloak/realms/CredentialIssuer/verifiable-credential/did:key:z6MkqmaCT2JqdUtLeKah7tEVfNXtDXtQyj4yxEgV11Y5CqUa/token"))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Object.class);
    }





}
