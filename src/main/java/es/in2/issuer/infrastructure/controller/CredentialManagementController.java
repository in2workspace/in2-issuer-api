package es.in2.issuer.infrastructure.controller;

import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.application.service.VerifiableCredentialIssuanceService;
import es.in2.issuer.domain.model.CredentialItem;
import es.in2.issuer.domain.exception.InvalidTokenException;
import es.in2.issuer.domain.model.*;
import es.in2.issuer.domain.service.CredentialManagementService;
import es.in2.issuer.domain.service.VerifiableCredentialService;
import es.in2.issuer.domain.util.Utils;
import es.in2.issuer.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/credentials")
@RequiredArgsConstructor
public class CredentialManagementController {

    private final VerifiableCredentialIssuanceService verifiableCredentialIssuanceService;
    private final CredentialManagementService credentialManagementService;

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

}
