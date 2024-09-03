package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.domain.model.dto.CredentialData;
import es.in2.issuer.domain.service.AccessTokenService;
import es.in2.issuer.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
    private final AccessTokenService accessTokenService;

    @Operation(
            summary = "Creates a withdrawn credential",
            description = "Generates a a withdrawn credential",
            tags = {SwaggerConfig.TAG_PUBLIC}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Returns Created when the creation was successfully"),
                    @ApiResponse(responseCode = "400", description = "The request is invalid or missing params Ensure the 'Authorization' header is set with a valid Bearer Token."),
                    @ApiResponse(responseCode = "500", description = "This response is returned when an unexpected server error occurs.")
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> issueCredential(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody CredentialData credentialData) {
        String processId = UUID.randomUUID().toString();
        return verifiableCredentialIssuanceWorkflow.completeWithdrawCredentialProcess(processId, credentialData.schema(), credentialData, authorizationHeader);
    }
}
