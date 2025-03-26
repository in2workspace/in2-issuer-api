package es.in2.issuer.authserver.application.workflow.impl;

import es.in2.issuer.authserver.domain.service.PreAuthCodeCacheStore;
import es.in2.issuer.authserver.domain.service.PreAuthCodeService;
import es.in2.issuer.shared.domain.model.dto.Grant;
import es.in2.issuer.shared.domain.model.dto.PreAuthCodeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreAuthCodeWorkflowImplTest {
    @Mock
    private PreAuthCodeService preAuthCodeService;

    @Mock
    private PreAuthCodeCacheStore preAuthCodeCacheStore;

    @InjectMocks
    PreAuthCodeWorkflowImpl preAuthCodeWorkflow;

    @Test
    void itShouldReturnPreAuthCode() {
        PreAuthCodeResponse expected = new PreAuthCodeResponse(
                new Grant("preAuthorizedCode",
                        new Grant.TxCode(5, "inputMode", "description")),
                "pin");

        when(preAuthCodeService.generatePreAuthCodeResponse(anyString())).thenReturn(Mono.just(expected));
        when(preAuthCodeCacheStore.save(anyString(), eq(expected.grant().preAuthorizedCode()), eq(expected.pin())))
                .thenReturn(Mono.just(expected.grant().preAuthorizedCode()));

        Mono<PreAuthCodeResponse> resultMono = preAuthCodeWorkflow.generatePreAuthCodeResponse();

        StepVerifier
                .create(resultMono)
                .assertNext(result ->
                        assertThat(result).isEqualTo(expected))
                .verifyComplete();

        verify(preAuthCodeCacheStore).save(anyString(), eq(expected.grant().preAuthorizedCode()), eq(expected.pin()));
    }
}