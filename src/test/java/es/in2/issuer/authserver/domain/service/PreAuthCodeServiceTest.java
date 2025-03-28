package es.in2.issuer.authserver.domain.service;

import es.in2.issuer.authserver.domain.service.impl.PreAuthCodeServiceImpl;
import es.in2.issuer.shared.domain.model.dto.PreAuthorizedCodeResponse;
import es.in2.issuer.shared.objectmother.PreAuthCodeResponseMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreAuthCodeServiceTest {

    @Mock
    private SecureRandom random;

    @Mock
    private PreAuthCodeCacheStore preAuthCodeCacheStore;

    @InjectMocks
    private PreAuthCodeServiceImpl preAuthCodeService;

    @Test
    void itShouldReturnPreAuthCode() {
        String expectedPreAuthCode = "1234";
        int randomNextInt = 5678;
        int expectedPin = randomNextInt + 1000;
        String expectedPinStr = String.valueOf(expectedPin);

        PreAuthorizedCodeResponse expected =
                PreAuthCodeResponseMother
                        .withPreAuthCodeAndPin(expectedPreAuthCode, expectedPinStr);

        when(random.nextInt(anyInt())).thenReturn(randomNextInt);
        when(preAuthCodeCacheStore.save(anyString(), anyString(), eq(expectedPinStr)))
                .thenReturn(Mono.just(expectedPreAuthCode));

        Mono<PreAuthorizedCodeResponse> resultMono = preAuthCodeService.generatePreAuthCodeResponse("");

        StepVerifier
                .create(resultMono)
                .assertNext(result ->
                        assertThat(result).isEqualTo(expected))
                .verifyComplete();
    }
}