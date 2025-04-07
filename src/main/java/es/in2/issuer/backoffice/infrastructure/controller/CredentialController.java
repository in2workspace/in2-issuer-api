package es.in2.issuer.backoffice.infrastructure.controller;

import es.in2.issuer.backoffice.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.backoffice.domain.model.dto.*;
import es.in2.issuer.backoffice.domain.service.AccessTokenService;
import es.in2.issuer.shared.infrastructure.config.SwaggerConfig;
import es.in2.issuer.shared.domain.model.dto.GlobalErrorMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/credentials")
@RequiredArgsConstructor
public class CredentialController {

    private final VerifiableCredentialIssuanceWorkflow verifiableCredentialIssuanceWorkflow;
    private final AccessTokenService accessTokenService;

    @Operation(
            summary = "Generate a new Verifiable Credential",
            description = "Generate a new Verifiable Credential and returns it with its id (nonce) assigned, lifetime in seconds and format",
            tags = {SwaggerConfig.TAG_PRIVATE}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "201", description = "Returns an URL with a linked ID parameter to retrieve the created Verifiable Credential."),
                    @ApiResponse(responseCode = "404", description = "Credential Request did not contain a proof, proof was invalid or no grants retrieved for the given user", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CredentialErrorResponse.class),
                            examples = @ExampleObject(name = "invalidOrMissingProof", value = "{\"error\": \"invalid_or_missing_proof\", \"description\": \"Credential Request did not contain a proof, or proof was invalid\"}"))),
                    @ApiResponse(responseCode = "500", description = "This response is returned when an unexpected server error occurs. It includes an error message if one is available.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GlobalErrorMessage.class)))
            }
    )
    @PostMapping(value = "/request-credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    // fixme: repensar esta API
    public Mono<ResponseEntity<VerifiableCredentialResponse>> createVerifiableCredential(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody CredentialRequest credentialRequest) {

        String processId = UUID.randomUUID().toString();

        return accessTokenService.getCleanBearerToken(authorizationHeader)
                .flatMap(token -> verifiableCredentialIssuanceWorkflow.generateVerifiableCredentialResponse(processId, credentialRequest, token))
                .map(verifiableCredentialResponse -> {
                    if (verifiableCredentialResponse.transactionId() != null) {
                        return ResponseEntity.status(HttpStatus.ACCEPTED).body(verifiableCredentialResponse);
                    } else {
                        return ResponseEntity.status(HttpStatus.OK).body(verifiableCredentialResponse);
                    }
                })
                .doOnSuccess(result -> log.info("VerifiableCredentialController - createVerifiableCredential(): " + result.toString()));
    }


    @PostMapping(value = "/request-deferred-credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<VerifiableCredentialResponse> getCredential(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody DeferredCredentialRequest deferredCredentialRequest) {
        String processId = UUID.randomUUID().toString();
        return verifiableCredentialIssuanceWorkflow.generateVerifiableCredentialDeferredResponse(processId, deferredCredentialRequest)
                .doOnNext(result -> log.info("VerifiableCredentialController - getDeferredCredential()"));
    }

    @PostMapping(value = "/request-batch-credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BatchCredentialResponse> createVerifiableCredentials(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody BatchCredentialRequest batchCredentialRequest) {
        return accessTokenService.getCleanBearerToken(authorizationHeader)
                .flatMap(token -> accessTokenService.getUserId(authorizationHeader)
                        .flatMap(userId -> verifiableCredentialIssuanceWorkflow.generateVerifiableCredentialBatchResponse(userId, batchCredentialRequest, token)))
                .doOnNext(result -> log.info("VerifiableCredentialController - createVerifiableCredential()"));
    }

}
