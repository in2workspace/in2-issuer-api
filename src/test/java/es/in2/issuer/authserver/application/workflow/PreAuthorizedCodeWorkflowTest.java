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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreAuthorizedCodeWorkflowTest {
    @Mock
    private PreAuthorizedCodeService preAuthorizedCodeService;

    @InjectMocks
    PreAuthorizedCodeWorkflowImpl preAuthorizedCodeWorkflow;

    @Test
    void itShouldReturnPreAuthorizedCode() {
        PreAuthorizedCodeResponse expected = PreAuthorizedCodeResponseMother.dummy();
        when(preAuthorizedCodeService.generatePreAuthorizedCodeResponse(anyString(), any())).thenReturn(Mono.just(expected));
        UUID credentialId = UUID.fromString("cfcd6d7c-5cc2-4601-a992-86f96afb0706");

        Mono<PreAuthorizedCodeResponse> resultMono = preAuthorizedCodeWorkflow
                .generatePreAuthorizedCodeResponse(
                        Mono.just(credentialId));

        StepVerifier
                .create(resultMono)
                .assertNext(result ->
                        assertThat(result).isEqualTo(expected))
                .verifyComplete();
    }
}