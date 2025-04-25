package es.in2.issuer.backend.backoffice.infrastructure.controller;

import es.in2.issuer.backend.shared.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.backend.shared.domain.model.dto.PreSubmittedCredentialRequest;
import es.in2.issuer.backend.shared.domain.service.AccessTokenService;
import es.in2.issuer.backend.shared.infrastructure.config.SwaggerConfig;
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
@RequestMapping
@RequiredArgsConstructor
public class PreSubmittedCredentialController {
    private final VerifiableCredentialIssuanceWorkflow verifiableCredentialIssuanceWorkflow;
    private final AccessTokenService accessTokenService;

    @Operation(
            summary = "Initiate Credential Issuance",
            description = "Starts the process of issuing a new credential. This endpoint handles the initial request for Verifiable Credential issuance, ensuring proper validation and preparation for subsequent steps in the issuance workflow.",
            tags = {SwaggerConfig.TAG_PUBLIC}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Returns Created when the creation was successfully"),
                    @ApiResponse(responseCode = "400", description = "The request is invalid or missing params Ensure the 'Authorization' header is set with a valid Bearer Token."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: Access is denied due to invalid or insufficient credentials."),
                    @ApiResponse(responseCode = "500", description = "This response is returned when an unexpected server error occurs.")
            }
    )
    @PostMapping("/backoffice/v1/issuances")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> internalIssueCredential(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody PreSubmittedCredentialRequest preSubmittedCredentialRequest) {
        String processId = UUID.randomUUID().toString();
        return accessTokenService.getCleanBearerToken(authorizationHeader).flatMap(
                token -> verifiableCredentialIssuanceWorkflow.completeIssuanceCredentialProcess(processId, preSubmittedCredentialRequest, token, null));
    }

    @PostMapping("/vci/v1/issuances")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> externalIssueCredential(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestHeader(name = "X-ID-TOKEN", required = false) String idToken,
            @RequestBody PreSubmittedCredentialRequest preSubmittedCredentialRequest) {
        String processId = UUID.randomUUID().toString();
        return accessTokenService.getCleanBearerToken(authorizationHeader).flatMap(
                token -> verifiableCredentialIssuanceWorkflow.completeIssuanceCredentialProcess(processId, preSubmittedCredentialRequest, token, idToken));
    }
}