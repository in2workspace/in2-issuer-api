package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.model.dto.CredentialDetails;
import es.in2.issuer.domain.model.dto.CredentialProcedures;
import es.in2.issuer.domain.service.CredentialProcedureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/procedures")
@RequiredArgsConstructor
public class CredentialManagementController {

    private final CredentialProcedureService credentialProcedureService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<CredentialProcedures> getAllProcedures(@RequestHeader(value = "X-SSL-Client-Cert") String clientCert) {
        log.debug(clientCert);
        return credentialProcedureService.getAllProceduresBasicInfoByOrganizationId(clientCert);
    }

    @GetMapping(value = "/{procedure_id}/credential-decoded", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<CredentialDetails> getProcedures(@PathVariable("procedure_id") String procedureId,
                                                 @RequestHeader(value = "X-SSL-Client-Cert") String clientCert) {
        log.debug(clientCert);
        return credentialProcedureService.getProcedureDetailByProcedureIdAndOrganizationId(clientCert, procedureId);
    }

}
