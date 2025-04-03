package es.in2.issuer.authserver.domain.service;

import es.in2.issuer.authserver.domain.service.impl.PreAuthorizedCodeServiceImpl;
import es.in2.issuer.shared.domain.model.dto.CredentialIdAndTxCode;
import es.in2.issuer.shared.domain.model.dto.PreAuthorizedCodeResponse;
import es.in2.issuer.shared.infrastructure.repository.CacheStoreRepository;
import es.in2.issuer.shared.objectmother.PreAuthorizedCodeResponseMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.SecureRandom;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreAuthorizedCodeServiceTest {

    @Mock
    private SecureRandom random;

    @Mock
    private CacheStoreRepository<CredentialIdAndTxCode> credentialIdAndTxCodeByPreAuthorizedCodeCacheStore;

    @InjectMocks
    private PreAuthorizedCodeServiceImpl preAuthorizedCodeService;

    @Test
    void itShouldReturnPreAuthorizedCode() {
        String expectedPreAuthorizedCode = "1234";
        int randomNextInt = 5678;
        int expectedTxCode = randomNextInt + 1000;
        String expectedTxCodeStr = String.valueOf(expectedTxCode);

        PreAuthorizedCodeResponse expected =
                PreAuthorizedCodeResponseMother
                        .withPreAuthorizedCodeAndPin(expectedPreAuthorizedCode, expectedTxCodeStr);

        when(random.nextInt(anyInt())).thenReturn(randomNextInt);
        UUID credentialId = UUID.fromString("2e10cbfc-b381-45ec-b987-0b1dd4ae4e10");
        when(credentialIdAndTxCodeByPreAuthorizedCodeCacheStore.add(anyString(), eq(new CredentialIdAndTxCode(credentialId, expectedTxCodeStr))))
                .thenReturn(Mono.just(expectedPreAuthorizedCode));

        Mono<PreAuthorizedCodeResponse> resultMono = preAuthorizedCodeService
                .generatePreAuthorizedCodeResponse("", credentialId);

        StepVerifier
                .create(resultMono)
                .assertNext(result ->
                        assertThat(result).isEqualTo(expected))
                .verifyComplete();

        verify(credentialIdAndTxCodeByPreAuthorizedCodeCacheStore, times(1))
                .add(anyString(), eq(new CredentialIdAndTxCode(credentialId, expectedTxCodeStr)));
    }
}