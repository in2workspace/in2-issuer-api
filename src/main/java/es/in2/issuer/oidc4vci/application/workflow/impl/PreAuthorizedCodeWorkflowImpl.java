package es.in2.issuer.oidc4vci.application.workflow.impl;

import es.in2.issuer.oidc4vci.application.workflow.PreAuthorizedCodeWorkflow;
import es.in2.issuer.oidc4vci.domain.service.PreAuthorizedCodeService;
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
    // todo: private final DeferredCredentialMetadataService deferredCredentialMetadataService;

    @Override
    public Mono<PreAuthorizedCodeResponse> generatePreAuthorizedCode(Mono<UUID> credentialIdMono) {
        String processId = UUID.randomUUID().toString();

        return preAuthorizedCodeService.generatePreAuthorizedCode(processId, credentialIdMono)
                // todo: updateCredentialMetadataWithPreAuthorizedCodeAndTxCode
//                  deferredCredentialMetadataService.updateAuthServerNonceByTransactionCode(
//                        transactionCode,
//                        preAuthorizedCodeResponse.grant().preAuthorizedCode()
//                        preAuthorizedCodeResponse.txCode()
//                  )
                .doFirst(() -> log.info("ProcessId: {} AuthServer: Starting PreAuthorizedCode generation", processId))
                .doOnSuccess(preAuthorizedCodeResponse ->
                        log.info(
                                "ProcessId: {} AuthServer: PreAuthorizedCode generation completed successfully",
                                processId));
    }
}
