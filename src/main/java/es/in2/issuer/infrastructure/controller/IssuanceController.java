package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.domain.model.dto.IssuanceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/vci/v1/issuances")
@RequiredArgsConstructor
public class IssuanceController {

    private final VerifiableCredentialIssuanceWorkflow verifiableCredentialIssuanceWorkflow;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> issueCredential(@RequestBody IssuanceRequest issuanceRequest) {
        String processId = UUID.randomUUID().toString();
        return verifiableCredentialIssuanceWorkflow
                .completeIssuanceCredentialProcess(processId, issuanceRequest.schema(), issuanceRequest);
    }

}