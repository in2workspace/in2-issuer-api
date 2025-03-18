package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.domain.model.dto.IssuanceRequest;
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
            summary = "Initiate Credential Issuance",
            description = "Starts the process of issuing a new credential. This endpoint handles the initial request for Verifiable Credential issuance, ensuring proper validation and preparation for subsequent steps in the issuance workflow.",
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
    public Mono<Void> internalIssueCredential(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody IssuanceRequest issuanceRequest) {
        String processId = UUID.randomUUID().toString();
        return accessTokenService.getCleanBearerToken(authorizationHeader).flatMap(
                token -> verifiableCredentialIssuanceWorkflow.completeIssuanceCredentialProcess(processId, issuanceRequest.schema(), issuanceRequest, token));
    }

    @PostMapping("/external")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> externalIssueCredential(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody IssuanceRequest issuanceRequest) {
        String processId = UUID.randomUUID().toString();
        return accessTokenService.getCleanBearerToken(authorizationHeader).flatMap(
                token -> verifiableCredentialIssuanceWorkflow.completeIssuanceCredentialProcess(processId, issuanceRequest.schema(), issuanceRequest, token));
    }
}