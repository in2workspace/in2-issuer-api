package es.in2.issuer.authserver.application.workflow.impl;

import es.in2.issuer.authserver.application.workflow.PreAuthorizedCodeWorkflow;
import es.in2.issuer.authserver.domain.service.PreAuthorizedCodeService;
import es.in2.issuer.shared.domain.model.dto.PreAuthorizedCodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreAuthorizedCodeWorkflowImpl implements PreAuthorizedCodeWorkflow {
    private final PreAuthorizedCodeService preAuthorizedCodeService;

    @Override
    public Mono<PreAuthorizedCodeResponse> generatePreAuthorizedCodeResponse(Mono<UUID> credentialIdMono) {
        String processId = UUID.randomUUID().toString();

        return credentialIdMono.flatMap(credentialId -> {
            log.info("ProcessId: {} AuthServer: Starting PreAuthorizedCode generation", processId);
            return preAuthorizedCodeService.generatePreAuthorizedCodeResponse(processId, credentialId);
        });
    }
}
