package es.in2.issuer.infrastructure.controller;

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

@Slf4j
@RestController
@RequestMapping("/token")
public class TokenController {

   /* private static final ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
            .maxConnections(500)
            .maxIdleTime(Duration.ofSeconds(50))
            .maxLifeTime(Duration.ofSeconds(300))
            .evictInBackground(Duration.ofSeconds(80))
            .build();

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Mono<Void> tokenBypass(@RequestBody Map<String, String> formData,
                                    @RequestHeader("Content-Type") String contentType) {

        log.info("Received token request with form data: {}", formData);
        log.info("Received token request with content type: {}", contentType);
*/
//        WebClient webClient = WebClient.builder()
//                .clientConnector(new ReactorClientHttpConnector(
//                        HttpClient.create(connectionProvider).followRedirect(false))
//                )
//                .build();

//        return webClient.post()
//                .uri(URI.create("https://in2-dome-marketplace-test.org/issuer-keycloak/realms/CredentialIssuer/verifiable-credential/did:key:z6MkqmaCT2JqdUtLeKah7tEVfNXtDXtQyj4yxEgV11Y5CqUa/token"))
//                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
//                .body(BodyInserters.fromFormData(new LinkedMultiValueMap<>()))
//                .retrieve()
//                .bodyToMono(Object.class);

        /*return Mono.empty();
    }*/

    @PostMapping(path = "/token")
    public Mono<String> handleData(ServerWebExchange exchange) {
        Mono<MultiValueMap<String, String>> formDataMono = exchange.getFormData();

        return formDataMono.map(formData -> {
            log.debug("\n==================================\n");
            log.debug("TokenFormData:[{}]", formData);
            log.debug("\n==================================\n");
            return formData.toString();
        });
    }
}
