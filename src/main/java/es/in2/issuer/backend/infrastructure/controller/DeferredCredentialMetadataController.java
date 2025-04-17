package es.in2.issuer.backend.infrastructure.controller;

import es.in2.issuer.backend.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.backend.domain.model.dto.AuthServerNonceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/deferred-credential-metadata")
@RequiredArgsConstructor
public class DeferredCredentialMetadataController {

    private final VerifiableCredentialIssuanceWorkflow verifiableCredentialIssuanceWorkflow;

    @PostMapping("/nonce")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> bindAccessTokenByPreAuthorizedCode (@RequestBody AuthServerNonceRequest authServerNonceRequest) {
        String processId = UUID.randomUUID().toString();
        return verifiableCredentialIssuanceWorkflow.bindAccessTokenByPreAuthorizedCode(processId,authServerNonceRequest);
    }

}
