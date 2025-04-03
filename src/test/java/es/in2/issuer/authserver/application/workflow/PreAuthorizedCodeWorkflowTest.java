package es.in2.issuer.authserver.application.workflow;

import es.in2.issuer.authserver.application.workflow.impl.PreAuthorizedCodeWorkflowImpl;
import es.in2.issuer.authserver.domain.service.PreAuthorizedCodeService;
import es.in2.issuer.shared.domain.model.dto.PreAuthorizedCodeResponse;
import es.in2.issuer.shared.objectmother.PreAuthorizedCodeResponseMother;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreAuthorizedCodeWorkflowTest {
    @Mock
    private PreAuthorizedCodeService preAuthorizedCodeService;

    @InjectMocks
    PreAuthorizedCodeWorkflowImpl preAuthorizedCodeWorkflow;

    @Test
    void itShouldReturnPreAuthorizedCode() {
        PreAuthorizedCodeResponse expected = PreAuthorizedCodeResponseMother.dummy();
        UUID credentialId = UUID.fromString("cfcd6d7c-5cc2-4601-a992-86f96afb0706");
        when(preAuthorizedCodeService.generatePreAuthorizedCodeResponse(anyString(), eq(credentialId)))
                .thenReturn(Mono.just(expected));

        Mono<PreAuthorizedCodeResponse> resultMono = preAuthorizedCodeWorkflow
                .generatePreAuthorizedCodeResponse(
                        Mono.just(credentialId));

        StepVerifier
                .create(resultMono)
                .assertNext(result ->
                        assertThat(result).isEqualTo(expected))
                .verifyComplete();

        verify(preAuthorizedCodeService, times(1))
                .generatePreAuthorizedCodeResponse(anyString(), eq(credentialId));
    }
}