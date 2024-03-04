package es.in2.issuer.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HttpUtilsTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.Builder webClientBuilder;

    @InjectMocks
    private HttpUtils httpUtils;

    @BeforeEach
    void setUp() {
        when(webClientBuilder.build()).thenReturn(webClient);
        httpUtils = new HttpUtils(webClientBuilder);
    }

    @Test
    void testGetRequest() {
        String url = "https://example.com";
        List<Map.Entry<String, String>> headers = Collections.emptyList();
        String responseBody = "MockedResponse";

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(Mockito.any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseBody));

        Mono<String> result = httpUtils.getRequest(url, headers);

        StepVerifier.create(result)
                .expectNext(responseBody)
                .verifyComplete();
    }

    @Test
    void testGetRequestErrorStatus() throws RuntimeException {
        String url = "https://example.com";
        List<Map.Entry<String, String>> headers = Collections.emptyList();

        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec<?> requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(url)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.headers(Mockito.any(Consumer.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        assertThrows(RuntimeException.class, () -> httpUtils.getRequest(url, headers));
    }

    @Test
    void testPostRequest() {
        String url = "https://example.com";
        List<Map.Entry<String, String>> headers = Collections.emptyList();
        String responseBody = "MockedResponse";

        String body = "MockedBody";

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(url)).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(Mockito.any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(body)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(Mockito.any(), Mockito.any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(responseBody));

        Mono<String> result = httpUtils.postRequest(url, headers, body);

        StepVerifier.create(result)
                .expectNext(responseBody)
                .verifyComplete();
    }

    @Test
    void testPostRequestErrorStatus() {
        String url = "https://example.com";
        List<Map.Entry<String, String>> headers = Collections.emptyList();

        String body = "MockedBody";

        WebClient.RequestBodyUriSpec requestBodyUriSpec = mock(WebClient.RequestBodyUriSpec.class);
        WebClient.RequestBodySpec requestBodySpec = mock(WebClient.RequestBodySpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(url)).thenReturn(requestBodySpec);
        when(requestBodySpec.headers(Mockito.any())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(body)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        assertThrows(RuntimeException.class, () -> httpUtils.postRequest(url, headers, body));
    }

    @ParameterizedTest
    @ValueSource(strings = {"example.com", "http://example.com", "https://example.com"})
    void ensureUrlHasProtocol_ReturnsUrlWithHttpsProtocol(String url) {
        // When
        String result = HttpUtils.ensureUrlHasProtocol(url);

        // Then
        if (url.startsWith("http://")) {
            assertEquals(url, result);
        } else if (url.startsWith("https://")) {
            assertEquals(url, result);
        } else {
            assertEquals("https://" + url, result);
        }
    }

}
