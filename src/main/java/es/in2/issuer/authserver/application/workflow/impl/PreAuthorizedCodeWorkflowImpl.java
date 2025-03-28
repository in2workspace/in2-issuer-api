package es.in2.issuer.authserver.application.workflow.impl;

import es.in2.issuer.authserver.application.workflow.PreAuthorizedCodeWorkflow;
import es.in2.issuer.authserver.domain.service.PreAuthCodeCacheStore;
import es.in2.issuer.authserver.domain.service.PreAuthCodeService;
import es.in2.issuer.shared.domain.model.dto.PreAuthCodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreAuthorizedCodeWorkflowImpl implements PreAuthorizedCodeWorkflow {
    private final PreAuthCodeService preAuthCodeService;
    private final PreAuthCodeCacheStore preAuthCodeCacheStore;

    @Override
    public Mono<PreAuthCodeResponse> generatePreAuthCodeResponse() {
        String processId = UUID.randomUUID().toString();
        log.info("ProcessId: {} AuthServer: Starting PreAuthCodeResponse generation", processId);

        return preAuthCodeService.generatePreAuthCodeResponse(processId)
                .flatMap(preAuthCodeResponse ->
                        preAuthCodeCacheStore.save(
                                        processId,
                                        preAuthCodeResponse.grant().preAuthorizedCode(),
                                        preAuthCodeResponse.pin())
                                .thenReturn(preAuthCodeResponse));
    }
}
