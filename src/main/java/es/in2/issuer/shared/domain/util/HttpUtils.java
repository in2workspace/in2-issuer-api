package es.in2.issuer.shared.domain.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.*;

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
                .bodyToMono(String.class);
    }

    public Mono<String> postRequest(@Nullable String url, List<Map.Entry<String, String>> headers, String body) {
        return webClient.post()
                .uri(Objects.requireNonNull(ensureUrlHasProtocol(url)))
                .headers(httpHeaders -> headers.forEach(entry -> httpHeaders.add(entry.getKey(), entry.getValue())))
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK, clientResponse ->
                        Mono.error(new RuntimeException("Error during post request:" + clientResponse.statusCode())))
                .bodyToMono(String.class);
    }

    public Mono<List<Map.Entry<String, String>>> prepareHeadersWithAuth(String token) {
        return Mono.fromCallable(() -> {
            List<Map.Entry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
            return headers;
        });
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
