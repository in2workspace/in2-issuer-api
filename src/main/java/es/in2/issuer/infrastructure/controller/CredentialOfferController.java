package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.service.CredentialOfferIssuanceService;
import es.in2.issuer.domain.model.CredentialErrorResponse;
import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.model.GlobalErrorMessage;
import es.in2.issuer.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CredentialOfferController {

    private final CredentialOfferIssuanceService credentialOfferIssuanceService;

    @Operation(
            summary = "Creates a credential offer",
            description = "Generates a Credential Offer of type LEAR CREDENTIAL for the Pre-Authorized Code Flow, using the user's JWT authentication token from the authorization server. " +
                    "The generated Credential Offer is stored with a unique nonce as the key for later retrieval.",
            tags = {SwaggerConfig.TAG_PUBLIC}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Returns Credential Offer URI for Pre-Authorized Code Flow using DOME standard",
                            content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(implementation = String.class),
                                    examples = @ExampleObject(name = "credentialOfferUri", value = "https://www.goodair.com/credential-offer?credential_offer_uri=https://www.goodair.com/credential-offer/5j349k3e3n23j"))
                    ),
                    @ApiResponse(
                            responseCode = "400", description = "The request is invalid or missing authentication credentials. Ensure the 'Authorization' header is set with a valid Bearer Token.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CredentialErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "The LEAR CREDENTIAL type is not supported", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CredentialErrorResponse.class),
                                    examples = @ExampleObject(name = "unsupportedCredentialType", value = "{\"error\": \"unsupported_credential_type\", \"description\": \"Credential Type '3213' not in credentials supported\"}"))
                    ),
                    @ApiResponse(
                            responseCode = "500", description = "This response is returned when an unexpected server error occurs. It includes an error message if one is available. Ensure the 'Authorization' header is set with a valid Bearer Token.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GlobalErrorMessage.class))
                    )
            }
    )
    @GetMapping("/api/v1/credential-offer")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> buildCredentialOffer(@PathParam("credential-type") String credentialType) {
        log.info("Building Credential Offer...");
        return credentialOfferIssuanceService.buildCredentialOfferUri(credentialType)
                .doOnSuccess(credentialOfferUri -> {
                            log.debug("Credential Offer URI created successfully: {}", credentialOfferUri);
                            log.info("Credential Offer created successfully.");
                        }
                );
    }

    @Operation(
            summary = "Returns a credential offer by ID",
            description = "This operation is used to retrieve a specific credential offer. Users should provide the ID of the desired credential offer in the URL path. The response will contain detailed information about the credential offer.",
            tags = {SwaggerConfig.TAG_PUBLIC}
    )
    @Parameter(
            name = "id",
            description = "The ID of the credential offer to retrieve",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = "string")
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returns the credential offer which matches the given ID in JSON format",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CustomCredentialOffer.class))
                    ),
                    @ApiResponse(
                            responseCode = "404", description = "The pre-authorized code is either expired, has already been used, or does not exist.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CredentialErrorResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500", description = "This response is returned when an unexpected server error occurs. It includes an error message if one is available.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GlobalErrorMessage.class))
                    )
            }
    )
    @GetMapping(value = "/credential-offer/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<CustomCredentialOffer> getCredentialOffer(@PathVariable("id") String id) {
        log.info("Retrieving Credential Offer...");
        return credentialOfferIssuanceService.getCustomCredentialOffer(id);
    }

}
