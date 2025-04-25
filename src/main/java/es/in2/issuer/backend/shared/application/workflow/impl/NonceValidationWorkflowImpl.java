package es.in2.issuer.backend.shared.application.workflow.impl;

import es.in2.issuer.backend.shared.application.workflow.NonceValidationWorkflow;
import es.in2.issuer.backend.shared.domain.service.NonceValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NonceValidationWorkflowImpl implements NonceValidationWorkflow {

    private final NonceValidationService nonceValidationService;

    @Override
    public Mono<Boolean> isValid(Mono<String> nonceMono) {
        String processId = UUID.randomUUID().toString();

        return nonceValidationService.isValid(processId, nonceMono)
                .doFirst(() -> log.info("ProcessId: {} AuthServer: Starting nonce validation", processId))
                .doOnSuccess(isValid ->
                        log.info(
                                "ProcessId: {} AuthServer: nonce validation completed  completed. Result: isValid = {}",
                                processId, isValid));
    }
}
