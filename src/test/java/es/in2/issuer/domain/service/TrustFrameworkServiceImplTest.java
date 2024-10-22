package es.in2.issuer.domain.service;

import es.in2.issuer.domain.exception.TrustFrameworkException;
import es.in2.issuer.domain.service.impl.TrustFrameworkServiceImpl;
import es.in2.issuer.infrastructure.config.TrustFrameworkConfig;
import es.in2.issuer.infrastructure.config.WebClientConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class TrustFrameworkServiceImplTest {

    @Mock
    private WebClientConfig webClientConfig;

    @Mock
    private TrustFrameworkConfig trustFrameworkConfig;

    @InjectMocks
    private TrustFrameworkServiceImpl service;

    @Test
    void registerParticipant() {
        String did = "did:key:1234";

        Mockito.when(trustFrameworkConfig.getTrustFrameworkUrl()).thenReturn("trust-framework-url");

        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.CREATED)
                .build();

        Mockito.when(exchangeFunction.exchange(any())).thenReturn(Mono.just(clientResponse));
        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        Mockito.when(webClientConfig.commonWebClient()).thenReturn(webClient);

        Mono<Void> result = service.registerParticipant(did);
        StepVerifier.create(result).verifyComplete();
    }

    @Test
    void registerParticipant_ShouldReturnError_WhenDIDAlreadyExists() {
        String did = "did:key:1234";

        Mockito.when(trustFrameworkConfig.getTrustFrameworkUrl()).thenReturn("trust-framework-url");

        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.CONFLICT).build();

        Mockito.when(exchangeFunction.exchange(any())).thenReturn(Mono.just(clientResponse));
        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        Mockito.when(webClientConfig.commonWebClient()).thenReturn(webClient);

        Mono<Void> result = service.registerParticipant(did);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(TrustFrameworkException.class);
                    assertThat(throwable.getMessage()).contains("DID already exists in the trusted list");
                })
                .verify();
    }

    @Test
    void registerParticipant_ShouldReturnError_WhenServerErrorOccurred() {
        String did = "did:key:1234";

        Mockito.when(trustFrameworkConfig.getTrustFrameworkUrl()).thenReturn("trust-framework-url");

        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.INTERNAL_SERVER_ERROR).build();

        Mockito.when(exchangeFunction.exchange(any())).thenReturn(Mono.just(clientResponse));
        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        Mockito.when(webClientConfig.commonWebClient()).thenReturn(webClient);

        Mono<Void> result = service.registerParticipant(did);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(TrustFrameworkException.class);
                    assertThat(throwable.getMessage()).contains("Server error occurred: 500 INTERNAL_SERVER_ERROR");
                })
                .verify();
    }

    @Test
    void registerParticipant_ShouldReturnError_WhenUnexpectedResponseStatus() {
        String did = "did:key:1234";

        Mockito.when(trustFrameworkConfig.getTrustFrameworkUrl()).thenReturn("trust-framework-url");

        ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
        ClientResponse clientResponse = ClientResponse.create(HttpStatus.NOT_FOUND).build();

        Mockito.when(exchangeFunction.exchange(any())).thenReturn(Mono.just(clientResponse));
        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        Mockito.when(webClientConfig.commonWebClient()).thenReturn(webClient);

        Mono<Void> result = service.registerParticipant(did);

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertThat(throwable).isInstanceOf(TrustFrameworkException.class);
                    assertThat(throwable.getMessage()).contains("Unexpected response status: 404 NOT_FOUND");
                })
                .verify();
    }
}