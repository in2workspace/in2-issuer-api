package es.in2.issuer.domain.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class HttpUtils {

    private final WebClient webClient;

    public HttpUtils(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<String> getRequest(@Nullable String url, List<Map.Entry<String, String>> headers) {
        return webClient.get()
                .uri(Objects.requireNonNull(ensureUrlHasProtocol(url)))
                .headers(httpHeaders -> headers.forEach(entry -> httpHeaders.add(entry.getKey(), entry.getValue())))
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK, clientResponse ->
                        Mono.error(new RuntimeException("Error during get request:" + clientResponse.statusCode())))
                .bodyToMono(String.class)
                .doOnNext(response -> logCRUD(url, headers, "", response, "GET"));
    }

    public Mono<String> postRequest(@Nullable String url, List<Map.Entry<String, String>> headers, String body) {
        return webClient.post()
                .uri(Objects.requireNonNull(ensureUrlHasProtocol(url)))
                .headers(httpHeaders -> headers.forEach(entry -> httpHeaders.add(entry.getKey(), entry.getValue())))
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK, clientResponse ->
                        Mono.error(new RuntimeException("Error during post request:" + clientResponse.statusCode())))
                .bodyToMono(String.class)
                .doOnNext(response -> logCRUD(url, headers, body, response, "POST"));
    }

    private void logCRUD(String url, List<Map.Entry<String, String>> headers, String requestBody, String responseBody, String method) {
        log.debug("********************************************************************************");
        log.debug(">>> METHOD: {}", method);
        log.debug(">>> URI: {}", url);
        log.debug(">>> HEADERS: {}", headers);
        log.debug(">>> BODY: {}", requestBody);
        log.debug("<<< BODY: {}", responseBody);
        log.debug("********************************************************************************");
    }

    public static String ensureUrlHasProtocol(String url) {
        if (url == null) {
            return null;
        }

        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }

        return url;
    }

}
