package es.in2.issuer.backend.backoffice.infrastructure.controller;

import es.in2.issuer.backend.backoffice.domain.model.entities.CloudProvider;
import es.in2.issuer.backend.backoffice.domain.service.CloudProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CloudProviderControllerTest {

    @Mock
    private CloudProviderService cloudProviderService;

    @InjectMocks
    private CloudProviderController controller;

    private static final String AUTH = "Bearer dummy-token";
    private CloudProvider sampleProvider;

    @BeforeEach
    void setUp() {
        UUID id = UUID.randomUUID();
        sampleProvider = CloudProvider.builder()
                .id(id)
                .provider("ProviderX")
                .url("https://provider.x")
                .authMethod("method")
                .authGrantType("grant")
                .requiresTOTP(true)
                .build();
    }

    @Test
    void createCloudProvider_success() {
        // arrange
        when(cloudProviderService.save(sampleProvider))
                .thenReturn(Mono.just(sampleProvider));

        // act
        Mono<ResponseEntity<CloudProvider>> result =
                controller.createCloudProvider(AUTH, sampleProvider);

        // assert
        StepVerifier.create(result)
                .assertNext(resp -> {
                    assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    assertThat(resp.getBody()).isEqualTo(sampleProvider);
                })
                .verifyComplete();

        verify(cloudProviderService).save(sampleProvider);
    }

    @Test
    void createCloudProvider_errorPropagates() {
        when(cloudProviderService.save(sampleProvider))
                .thenReturn(Mono.error(new IllegalStateException("fail")));

        StepVerifier.create(controller.createCloudProvider(AUTH, sampleProvider))
                .expectErrorMatches(e ->
                        e instanceof IllegalStateException &&
                                e.getMessage().equals("fail")
                )
                .verify();

        verify(cloudProviderService).save(sampleProvider);
    }

    @Test
    void getAllCloudProviders_success() {
        CloudProvider other = CloudProvider.builder()
                .id(UUID.randomUUID())
                .provider("Other")
                .url("u")
                .authMethod("m")
                .authGrantType("g")
                .requiresTOTP(false)
                .build();

        when(cloudProviderService.findAll())
                .thenReturn(Flux.just(sampleProvider, other));

        Flux<CloudProvider> result = controller.getAllCloudProviders(AUTH);

        StepVerifier.create(result)
                .assertNext(cp -> assertThat(cp).isEqualTo(sampleProvider))
                .assertNext(cp -> assertThat(cp).isEqualTo(other))
                .verifyComplete();

        verify(cloudProviderService).findAll();
    }

    @Test
    void getAllCloudProviders_errorPropagates() {
        when(cloudProviderService.findAll())
                .thenReturn(Flux.error(new RuntimeException("oops")));

        StepVerifier.create(controller.getAllCloudProviders(AUTH))
                .expectErrorMessage("oops")
                .verify();

        verify(cloudProviderService).findAll();
    }
}
