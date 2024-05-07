package es.in2.issuer.infrastructure.controller;

import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.application.service.VerifiableCredentialIssuanceService;
import es.in2.issuer.domain.exception.InvalidTokenException;
import es.in2.issuer.domain.model.*;
import es.in2.issuer.domain.service.VerifiableCredentialService;
import es.in2.issuer.domain.util.Utils;
import es.in2.issuer.infrastructure.config.SwaggerConfig;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.text.ParseException;

import static es.in2.issuer.domain.util.Constants.REQUEST_ERROR_MESSAGE;
import static es.in2.issuer.domain.util.Utils.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/vc")
@RequiredArgsConstructor
public class VerifiableCredentialController {

    private final VerifiableCredentialService verifiableCredentialService;
    private final VerifiableCredentialIssuanceService verifiableCredentialIssuanceService;

    @Operation(
            summary = "Generate a new Verifiable Credential",
            description = "Generate a new Verifiable Credential and returns it with its id (nonce) assigned, lifetime in seconds and format",
            tags = {SwaggerConfig.TAG_PRIVATE}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Returns an URL with a linked ID parameter to retrieve the created Verifiable Credential."
                    ),
                    @ApiResponse(responseCode = "404", description = "Credential Request did not contain a proof, proof was invalid or no grants retrieved for the given user", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CredentialErrorResponse.class),
                                    examples = @ExampleObject(name = "invalidOrMissingProof", value = "{\"error\": \"invalid_or_missing_proof\", \"description\": \"Credential Request did not contain a proof, or proof was invalid\"}"))
                    ),
                    @ApiResponse(responseCode = "500", description = "This response is returned when an unexpected server error occurs. It includes an error message if one is available.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GlobalErrorMessage.class))
                    )
            }
    )
    @PostMapping(value = "/credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<VerifiableCredentialResponse> createVerifiableCredential(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody CredentialRequest credentialRequest) {
        return getCleanBearerToken(authorizationHeader)
                .flatMap(token -> getUserIdFromToken(token)
                        .flatMap(userId -> verifiableCredentialIssuanceService.generateVerifiableCredentialResponse(userId, credentialRequest, token)))
                .doOnNext(result -> log.info("VerifiableCredentialController - createVerifiableCredential()"))
                .onErrorMap(e -> new RuntimeException(REQUEST_ERROR_MESSAGE, e));
    }

    @PostMapping(value = "/deferred_credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<VerifiableCredentialResponse> getCredential(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody DeferredCredentialRequest deferredCredentialRequest) {
        return getCleanBearerToken(authorizationHeader)
                .flatMap(token -> getUserIdFromToken(token)
                        .flatMap(userId -> verifiableCredentialIssuanceService.generateVerifiableCredentialDeferredResponse(userId, deferredCredentialRequest, token)))
                .doOnNext(result -> log.info("VerifiableCredentialController - getCredential()"))
                .onErrorMap(e -> new RuntimeException(REQUEST_ERROR_MESSAGE, e));
    }

    @PostMapping(value = "/batch_credential", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<BatchCredentialResponse> createVerifiableCredentials(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody BatchCredentialRequest batchCredentialRequest) {
        return getCleanBearerToken(authorizationHeader)
                .flatMap(token -> getUserIdFromToken(token)
                        .flatMap(userId -> verifiableCredentialIssuanceService.generateVerifiableCredentialBatchResponse(userId, batchCredentialRequest, token)))
                .doOnNext(result -> log.info("VerifiableCredentialController - createVerifiableCredential()"))
                .onErrorMap(e -> new RuntimeException(REQUEST_ERROR_MESSAGE, e));
    }
}
