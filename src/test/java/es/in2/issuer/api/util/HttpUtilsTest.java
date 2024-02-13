package es.in2.issuer.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    @Test
    void ensureUrlHasProtocol_WithoutProtocol_ReturnsHttpsUrl() {
        // Given
        String url = "example.com";

        // When
        String result = HttpUtils.ensureUrlHasProtocol(url);

        // Then
        assertEquals("https://example.com", result);
    }

    @Test
    void ensureUrlHasProtocol_WithHttpProtocol_ReturnsOriginalUrl() {
        // Given
        String url = "http://example.com";

        // When
        String result = HttpUtils.ensureUrlHasProtocol(url);

        // Then
        assertEquals("http://example.com", result);
    }

    @Test
    void ensureUrlHasProtocol_WithHttpsProtocol_ReturnsOriginalUrl() {
        // Given
        String url = "https://example.com";

        // When
        String result = HttpUtils.ensureUrlHasProtocol(url);

        // Then
        assertEquals("https://example.com", result);
    }


}
