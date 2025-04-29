package es.in2.issuer.backend.backoffice.infrastructure.controller;

import es.in2.issuer.backend.backoffice.application.workflow.CredentialActionsWorkflow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/backoffice/v1/credential-records/{recordId}/actions")
@RequiredArgsConstructor
public class CredentialActionsController {

    private final CredentialActionsWorkflow credentialActionsWorkflow;

    @PostMapping(value= "/send-reminder", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> sendReminder(@PathVariable String recordId) {
        String processId = UUID.randomUUID().toString();
        return credentialActionsWorkflow
                .sendReminder(processId, recordId)
                .doFirst(() ->
                        log.info("ProcessID: {} - Sending reminder for Credential Record with ID {}...", processId, recordId))
                .doOnSuccess(result ->
                        log.info("ProcessID: {} - Reminder sent successfully for Credential Record with ID {}", processId, recordId));
    }

    @PostMapping("/manual-sign")
    public Mono<Void> manualSign(@PathVariable String recordId) {
        // firma manual
        return Mono.empty();
    }

    @PostMapping("/revoke")
    public Mono<Void> revokeCredential(@PathVariable String recordId) {
        // revocar
        return Mono.empty();
    }

    @PostMapping("/renew")
    public Mono<Void> renewCredential(@PathVariable String recordId) {
        // renovar
        return Mono.empty();
    }

}
