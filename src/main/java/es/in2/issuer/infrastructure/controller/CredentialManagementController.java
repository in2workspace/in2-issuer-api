package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.model.*;
import es.in2.issuer.domain.service.AccessTokenService;
import es.in2.issuer.domain.service.CredentialManagementService;
import es.in2.issuer.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CredentialManagementController {

    private final CredentialManagementService credentialManagementService;
    private final AccessTokenService accessTokenService;

    @Operation(
            summary = "Get the credentials committed by the current user",
            description = "Get the credentials committed by the current user",
            tags = {SwaggerConfig.TAG_PRIVATE}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returns credentials committed by the current user."
                    ),
                    @ApiResponse(responseCode = "404", description = "Credential Request did not contain a proof, proof was invalid or no grants retrieved for the given user", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CredentialErrorResponse.class),
                            examples = @ExampleObject(name = "invalidOrMissingProof", value = "{\"error\": \"invalid_or_missing_proof\", \"description\": \"Credential Request did not contain a proof, or proof was invalid\"}"))
                    ),
                    @ApiResponse(responseCode = "500", description = "This response is returned when an unexpected server error occurs. It includes an error message if one is available.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GlobalErrorMessage.class))
                    )
            }
    )
    @GetMapping(value = "/api/v1/credentials", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CredentialItem> getCredentials(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "modifiedAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return accessTokenService.getUserIdFromHeader(authorizationHeader)
                .flatMapMany(userId -> credentialManagementService.getCredentials(userId, page, size, sort, direction))
                .doOnEach(credential -> log.info("CredentialManagementController - getCredentials(): {}", credential.get())); // Handle all errors from the stream
    }

    @GetMapping(value = "/api/v1/pending_credentials", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<PendingCredentials> getUnsignedCredentials(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "modifiedAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return accessTokenService.getUserIdFromHeader(authorizationHeader)
                .flatMap(userId -> credentialManagementService.getPendingCredentials(userId, page, size, sort, direction))
                .doOnEach(credential -> log.info("CredentialManagementController - getUnsignedCredentials(): {}", credential.get()));
    }

    @GetMapping(value = "/api/v1/credentials/{credentialId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<CredentialItem> getCredential(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable UUID credentialId) {
        return accessTokenService.getCleanBearerToken(authorizationHeader)
                .flatMap(token -> accessTokenService.getUserIdFromHeader(authorizationHeader)
                        .flatMap(userId -> credentialManagementService.getCredential(credentialId, userId)))
                .doOnNext(result -> log.info("CredentialManagementController - getCredential()"));
    }

    @PostMapping(value = "/api/v1/credentials", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateCredentials(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @RequestBody SignedCredentials signedCredentials) {
        return Mono.defer(() -> {

            Mono<String> userIdMono = Mono.just(authorizationHeader)
                    .flatMap(accessTokenService::getUserIdFromHeader);

            return userIdMono.flatMap(userId -> credentialManagementService.updateCredentials(signedCredentials, userId))
                    .doOnSuccess(result -> log.info("VerifiableCredentialController - signVerifiableCredentials() completed"));
        });
    }

}
