package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.CredentialManagementWorkflow;
import es.in2.issuer.domain.model.dto.PendingCredentials;
import es.in2.issuer.domain.model.dto.SignedCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CredentialManagementController {

    private final CredentialManagementWorkflow credentialManagementWorkflow;

//    @Operation(
//            summary = "Get the credentials committed by the current user",
//            description = "Get the credentials committed by the current user",
//            tags = {SwaggerConfig.TAG_PRIVATE}
//    )
//    @ApiResponses(
//            value = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "Returns credentials committed by the current user."
//                    ),
//                    @ApiResponse(responseCode = "404", description = "Credential Request did not contain a proof, proof was invalid or no grants retrieved for the given user", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CredentialErrorResponse.class),
//                            examples = @ExampleObject(name = "invalidOrMissingProof", value = "{\"error\": \"invalid_or_missing_proof\", \"description\": \"Credential Request did not contain a proof, or proof was invalid\"}"))
//                    ),
//                    @ApiResponse(responseCode = "500", description = "This response is returned when an unexpected server error occurs. It includes an error message if one is available.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GlobalErrorMessage.class))
//                    )
//            }
//    )
//    @GetMapping(value = "/api/v1/credentials", produces = MediaType.APPLICATION_JSON_VALUE)
//    public Flux<CredentialItem> getCredentials(
//            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "modifiedAt") String sort,
//            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
//        return accessTokenService.getUserIdFromHeader(authorizationHeader)
//                .flatMapMany(userId -> credentialManagementService.getCredentials(userId, page, size, sort, direction))
//                .doOnEach(credential -> log.info("CredentialManagementController - getCredentials(): {}", credential.get())); // Handle all errors from the stream
//    }

    @GetMapping(value = "/pending_credentials", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<PendingCredentials> getUnsignedCredentials(
            @RequestHeader(value = "X-SSL-Client-Cert") String clientCert)
    {
        log.debug(clientCert);
        return credentialManagementWorkflow.getPendingCredentialsByOrganizationId(clientCert);
    }

//    @GetMapping(value = "/api/v1/credentials/{credentialId}", produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseStatus(HttpStatus.OK)
//    public Mono<CredentialItem> getCredential(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable UUID credentialId) {
//        return accessTokenService.getCleanBearerToken(authorizationHeader)
//                .flatMap(token -> accessTokenService.getUserIdFromHeader(authorizationHeader)
//                        .flatMap(userId -> credentialManagementService.getCredential(credentialId, userId)))
//                .doOnNext(result -> log.info("CredentialManagementController - getCredential()"));
//    }
//
    @PostMapping(value = "/update-pending-credentials", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateCredentials(@RequestBody SignedCredentials signedCredentials) {
        return credentialManagementWorkflow.updateSignedCredentials(signedCredentials);
    }

}
