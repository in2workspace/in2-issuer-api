package es.in2.issuer.infrastructure.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.*;
import es.in2.issuer.domain.service.CredentialManagementService;
import es.in2.issuer.domain.service.VerifiableCredentialService;
import es.in2.issuer.domain.util.HttpUtils;
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
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static es.in2.issuer.domain.util.Constants.REQUEST_ERROR_MESSAGE;
import static es.in2.issuer.domain.util.Utils.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/credentials")
@RequiredArgsConstructor
public class CredentialManagementController {

    private final CredentialManagementService credentialManagementService;
    private final VerifiableCredentialService verifiableCredentialService;
    private final HttpUtils httpUtils;
    private final ObjectMapper objectMapper;

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
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CredentialItem> getCredentials(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "modifiedAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return getCleanBearerToken(authorizationHeader)
                .flatMap(Utils::getUserIdFromToken)
                .flatMapMany(userId -> credentialManagementService.getCredentials(userId, page, size, sort, direction))
                .doOnEach(signal -> {  // Enhanced logging for each signal including onComplete, onError
                    if (signal.isOnNext()) {
                        log.info("CredentialManagementController - getCredential(): {}", signal.get());
                    } else if (signal.isOnError()) {
                        log.error("Error fetching credentials", signal.getThrowable());
                    }
                })
                .onErrorMap(e -> new RuntimeException(REQUEST_ERROR_MESSAGE, e)); // Handle all errors from the stream
    }

    @GetMapping(value = "/{credentialId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<CredentialItem> getCredential(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable UUID credentialId) {
        return getCleanBearerToken(authorizationHeader)
                .flatMap(token -> getUserIdFromToken(token)
                        .flatMap(userId -> credentialManagementService.getCredential(credentialId, userId)))
                .doOnNext(result -> log.info("CredentialManagementController - getCredential()"))
                .onErrorMap(e -> new RuntimeException(REQUEST_ERROR_MESSAGE, e));
    }

    @PostMapping(value = "/sign/{credentialId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> signVerifiableCredentials(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathVariable UUID credentialId, @RequestBody String unsignedCredential) {
        return Mono.defer(() -> {
                        String token = String.valueOf(getCleanBearerToken(authorizationHeader));
                        String userId = String.valueOf(getUserIdFromToken(token));

                        // Generate deferred VC Payload reactively
                        return verifiableCredentialService.generateDeferredVcPayLoad(unsignedCredential)
                                .flatMap(vcPayload -> {
                                    //::::::::::::: mock of the local signature using a remote DSS :::::::::::::
                                    // Create request object
                                    SignatureRequest signatureRequest = new SignatureRequest(
                                            new SignatureConfiguration(SignatureType.JADES, Collections.emptyMap()),
                                            vcPayload);
                                    String signatureRequestJSON = null;
                                    try {
                                        signatureRequestJSON = objectMapper.writeValueAsString(signatureRequest);
                                    } catch (JsonProcessingException e) {
                                        return Mono.error(new RuntimeException(e));
                                    }
                                    // Prepare headers
                                    List<Map.Entry<String, String>> headers = new ArrayList<>();
                                    headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
                                    headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
                                    // Execute request to the remote signature
                                    return httpUtils.postRequest("http://localhost:8050/api/v1/signature/sign", headers, signatureRequestJSON)
                                            .flatMap(response -> {
                                                log.info("Received response: " + response);

                                                // Extract signed credential from response
                                                JsonNode responseNode = null;
                                                try {
                                                    responseNode = objectMapper.readTree(response);
                                                } catch (JsonProcessingException e) {
                                                    return Mono.error(new RuntimeException(e));
                                                }
                                                String signedCredential = responseNode.get("data").asText();
                                   //::::::::::::: end mock of the local signature using a remote DSS :::::::::::::
                                                // Update the credential
                                                return credentialManagementService.updateCredential(signedCredential, credentialId, userId);
                                            });
                                });
                })
                .doOnSuccess(result -> log.info("VerifiableCredentialController - signVerifiableCredentials()"))
                .onErrorMap(e -> new RuntimeException(REQUEST_ERROR_MESSAGE, e));
    }

}
