package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/sign-credential")
@RequiredArgsConstructor
public class CredentialSignerController {

    private final CredentialSignerWorkflow credentialSignerWorkflow;

    @PostMapping(value = "/{procedure_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> createVerifiableCredential(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable("procedure_id") String procedureId) {
        return credentialSignerWorkflow.signCredential(authorizationHeader, procedureId);
    }
}
