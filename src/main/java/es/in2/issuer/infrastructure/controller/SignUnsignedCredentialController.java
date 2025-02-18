package es.in2.issuer.infrastructure.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import es.in2.issuer.domain.model.dto.ProcedureIdRequest;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> SignUnsignedCredential(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody ProcedureIdRequest procedureIdRequest) {
        return credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(authorizationHeader, procedureIdRequest.procedureId(), JWT_VC).then();
     }

}
