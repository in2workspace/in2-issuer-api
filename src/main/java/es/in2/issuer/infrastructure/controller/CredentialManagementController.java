package es.in2.issuer.infrastructure.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.application.service.VerifiableCredentialIssuanceService;
import es.in2.issuer.domain.model.CredentialItem;
import es.in2.issuer.domain.exception.InvalidTokenException;
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
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/credentials")
@RequiredArgsConstructor
public class CredentialManagementController {

    private final VerifiableCredentialIssuanceService verifiableCredentialIssuanceService;
    private final CredentialManagementService credentialManagementService;
    private final VerifiableCredentialService verifiableCredentialService;
    private final HttpUtils httpUtils;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
            ServerWebExchange exchange,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "modifiedAt") String sort,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        return Mono.defer(() -> {
                    try {
                        SignedJWT token = Utils.getToken(exchange);
                        String userId = token.getJWTClaimsSet().getClaim("sub").toString();
                        return Mono.just(userId);
                    } catch (InvalidTokenException | ParseException e) {
                        return Mono.error(new RuntimeException("Invalid or missing token", e));
                    }
                })
                .flatMapMany(userId -> credentialManagementService.getCredentials(userId, page, size, sort, direction))
                .doOnNext(result -> log.info("Accessed getCredentials"))
                .onErrorMap(e -> new RuntimeException("Error processing the request", e));
    }

    @GetMapping(value = "/{credentialId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<CredentialItem> getCredential(@PathVariable UUID credentialId, ServerWebExchange exchange) {
        return Mono.defer(() -> {
                    try {
                        SignedJWT token = Utils.getToken(exchange);
                        String userId = token.getJWTClaimsSet().getClaim("sub").toString();
                        return credentialManagementService.getCredential(credentialId, userId);
                    } catch (InvalidTokenException | ParseException e) {
                        return Mono.error(e);
                    }
                }).doOnNext(result -> log.info("CredentialManagementController - getCredential()"))
                .onErrorMap(e -> new RuntimeException("Error processing the request", e));
    }

    @PostMapping(value = "/sign/{credentialId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> signVerifiableCredentials(@PathVariable UUID credentialId, @RequestBody String unsignedCredential, ServerWebExchange exchange) {
        return Mono.defer(() -> {
                    try {
                        SignedJWT token = Utils.getToken(exchange);
                        String userId = token.getJWTClaimsSet().getClaim("sub").toString();

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
                                        throw new RuntimeException(e);
                                    }
                                    // Prepare headers
                                    List<Map.Entry<String, String>> headers = new ArrayList<>();
                                    headers.add(new AbstractMap.SimpleEntry<>(HttpHeaders.AUTHORIZATION, "Bearer " + token.getParsedString()));
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
                                                    throw new RuntimeException(e);
                                                }
                                                String signedCredential = responseNode.get("data").asText();
                                    //::::::::::::: end mock of the local signature using a remote DSS :::::::::::::
                                                // Update the credential
                                                return credentialManagementService.updateCredential(signedCredential, credentialId, userId);
                                            });
                                });
                    } catch (InvalidTokenException | ParseException e) {
                        return Mono.error(e);
                    }
                })
                .doOnNext(result -> log.info("VerifiableCredentialController - signVerifiableCredentials()"))
                .onErrorMap(e -> new RuntimeException("Error processing the request", e));
    }

}
