package es.in2.issuer.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import es.in2.issuer.domain.model.dto.ProcedureIdRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import static es.in2.issuer.domain.util.Constants.JWT_VC;


@Slf4j
@RestController
@RequestMapping("/api/v1/retry-sign-credential")
@RequiredArgsConstructor
public class SignUnsignedCredentialController {

    private final CredentialSignerWorkflow credentialSignerWorkflow;

    @PostMapping(value = "/{procedure_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> signUnsignedCredential(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable String procedure_id) {
        return credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(authorizationHeader, procedure_id, JWT_VC).then();
     }

}
