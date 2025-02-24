package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.infrastructure.repository.CredentialProcedureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import es.in2.issuer.application.workflow.CredentialSignerWorkflow;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.JWT_VC;


@Slf4j
@RestController
@RequestMapping("/api/v1/retry-sign-credential")
@RequiredArgsConstructor
public class SignUnsignedCredentialController {

    private final CredentialSignerWorkflow credentialSignerWorkflow;
    private final CredentialProcedureService credentialProcedureService;
    private final CredentialProcedureRepository credentialProcedureRepository;

    @PostMapping(value = "/{procedure_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> signUnsignedCredential(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable("procedure_id") String procedureId) {
        return credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(authorizationHeader, procedureId, JWT_VC)
                .then(credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId))
                .then(
                        credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId))
                                .flatMap(credentialProcedure ->
                                        Mono.fromRunnable(() ->
                                                credentialProcedure.setUpdatedAt(Timestamp.from(Instant.now()))
                                        )
                                )
                ).then();
     }
}
