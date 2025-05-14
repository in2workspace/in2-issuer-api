package es.in2.issuer.backend.backoffice.domain.service.impl;

import es.in2.issuer.backend.backoffice.domain.model.entities.CloudProvider;
import es.in2.issuer.backend.backoffice.domain.repository.CloudProviderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class CloudProviderServiceImplTest {

    @Mock
    private CloudProviderRepository repository;

    @InjectMocks
    private CloudProviderServiceImpl service;

    private CloudProvider sampleProvider;
    private UUID id;

    @BeforeEach
    void setUp() {
        id = UUID.randomUUID();
        sampleProvider = CloudProvider.builder()
                .id(id)
                .provider("Prov")
                .url("https://example.com")
                .authMethod("method")
                .authGrantType("grant")
                .requiresTOTP(true)
                .build();
    }

    @Test
    void save_delegatesToRepository() {
        when(repository.save(sampleProvider)).thenReturn(Mono.just(sampleProvider));

        Mono<CloudProvider> result = service.save(sampleProvider);

        StepVerifier.create(result)
                .assertNext(cp -> assertThat(cp).isEqualTo(sampleProvider))
                .verifyComplete();

        verify(repository).save(sampleProvider);
    }

    @Test
    void findAll_delegatesToRepository() {
        CloudProvider other = CloudProvider.builder()
                .id(UUID.randomUUID())
                .provider("X")
                .url("u")
                .authMethod("m")
                .authGrantType("g")
                .requiresTOTP(false)
                .build();
        when(repository.findAll()).thenReturn(Flux.just(sampleProvider, other));

        Flux<CloudProvider> result = service.findAll();

        StepVerifier.create(result)
                .assertNext(cp -> assertThat(cp).isEqualTo(sampleProvider))
                .assertNext(cp -> assertThat(cp).isEqualTo(other))
                .verifyComplete();

        verify(repository).findAll();
    }

    @Test
    void requiresTOTP_true() {
        when(repository.findById(id)).thenReturn(Mono.just(sampleProvider));

        StepVerifier.create(service.requiresTOTP(id))
                .expectNext(true)
                .verifyComplete();

        verify(repository).findById(id);
    }

    @Test
    void requiresTOTP_falseWhenProviderSaysFalse() {
        CloudProvider noTotp = CloudProvider.builder()
                .id(id)
                .provider("P")
                .url("u")
                .authMethod("m")
                .authGrantType("g")
                .requiresTOTP(false)
                .build();
        when(repository.findById(id)).thenReturn(Mono.just(noTotp));

        StepVerifier.create(service.requiresTOTP(id))
                .expectNext(false)
                .verifyComplete();

        verify(repository).findById(id);
    }

    @Test
    void requiresTOTP_falseWhenNotFound() {
        when(repository.findById(id)).thenReturn(Mono.empty());

        StepVerifier.create(service.requiresTOTP(id))
                .expectNext(false)
                .verifyComplete();

        verify(repository).findById(id);
    }

    @Test
    void findById_delegatesToRepository() {
        when(repository.findById(id)).thenReturn(Mono.just(sampleProvider));

        StepVerifier.create(service.findById(id))
                .assertNext(cp -> assertThat(cp).isEqualTo(sampleProvider))
                .verifyComplete();

        verify(repository).findById(id);
    }
}

