package es.in2.issuer.backend.backoffice.infrastructure.controller;

import es.in2.issuer.backend.backoffice.application.workflow.CredentialRecordWorkflow;
import es.in2.issuer.backend.shared.domain.model.dto.CredentialDetails;
import es.in2.issuer.backend.shared.domain.model.dto.CredentialProcedures;
import es.in2.issuer.backend.shared.domain.model.dto.PreSubmittedCredentialRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/backoffice/v1/credential-records")
@RequiredArgsConstructor
public class CredentialRecordController {

    private final CredentialRecordWorkflow credentialRecordWorkflow;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> createCredentialRecord(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody PreSubmittedCredentialRequest preSubmittedCredentialRequest) {
        String processId = UUID.randomUUID().toString();
        return credentialRecordWorkflow
                .createCredentialRecord(processId, preSubmittedCredentialRequest, authorizationHeader)
                .doFirst(() ->
                        log.info("ProcessID: {} - Registering new Credential Record...", processId))
                .doOnSuccess(result ->
                        log.info("ProcessID: {} - Credential Record registered successfully", processId));
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<CredentialProcedures> listCredentialRecords(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String processId = UUID.randomUUID().toString();
        return credentialRecordWorkflow.getAllCredentialRecords(processId, authorizationHeader)
                .doFirst(() ->
                        log.info("ProcessID: {} - Listing all Credential Records...", processId))
                .doOnSuccess(result ->
                        log.info("ProcessID: {} - Credential Records listed successfully", processId));

    }

    @GetMapping("/{recordId}/credential")
    @ResponseStatus(HttpStatus.OK)
    public Mono<CredentialDetails> getCredentialDetailsByCredentialRecordId(@PathVariable String recordId, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String processId = UUID.randomUUID().toString();
        return credentialRecordWorkflow.getCredentialRecordById(processId, recordId, authorizationHeader)
                .doFirst(() ->
                        log.info("ProcessID: {} - Retrieving Credential Record with ID {}...", processId, recordId))
                .doOnSuccess(result ->
                        log.info("ProcessID: {} - Credential Record with ID {} retrieved successfully", processId, recordId));
    }

}
