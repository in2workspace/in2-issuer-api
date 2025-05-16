package es.in2.issuer.backend.backoffice.infrastructure.controller;

import es.in2.issuer.backend.shared.application.workflow.CredentialIssuanceWorkflow;
import es.in2.issuer.backend.shared.domain.model.dto.PreSubmittedDataCredential;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class IssuanceController {

    private final CredentialIssuanceWorkflow credentialIssuanceWorkflow;

    @PostMapping("/backoffice/v1/issuances")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> internalIssueCredential(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
                                              @RequestBody @Valid PreSubmittedDataCredential preSubmittedDataCredential) {
        String processId = UUID.randomUUID().toString();
        return credentialIssuanceWorkflow.execute(processId, preSubmittedDataCredential, bearerToken, null);
    }

    @PostMapping("/vci/v1/issuances")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> externalIssueCredential(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken,
                                              @RequestHeader(name = "X-Id-Token", required = false) String idToken,
                                              @RequestBody @Valid PreSubmittedDataCredential preSubmittedDataCredential) {
        String processId = UUID.randomUUID().toString();
        return credentialIssuanceWorkflow.execute(processId, preSubmittedDataCredential, bearerToken, idToken);
    }

}