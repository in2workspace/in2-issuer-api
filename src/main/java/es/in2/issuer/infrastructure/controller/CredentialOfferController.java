package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.CredentialOfferIssuanceWorkflow;
import es.in2.issuer.domain.model.dto.CredentialErrorResponse;
import es.in2.issuer.domain.model.dto.GlobalErrorMessage;
import es.in2.issuer.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/credential-offer")
@RequiredArgsConstructor
public class CredentialOfferController {

    private final CredentialOfferIssuanceWorkflow credentialOfferIssuanceWorkflow;

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
    @GetMapping("/{transaction_code}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<String> buildCredentialOffer(@PathVariable("transaction_code") String transactionCode) {
        log.info("Building Credential Offer...");
        String processId = UUID.randomUUID().toString();
        return credentialOfferIssuanceWorkflow.buildCredentialOfferUri(processId, transactionCode)
                .doOnSuccess(credentialOfferUri -> {
                            log.debug("Credential Offer URI created successfully: {}", credentialOfferUri);
                            log.info("Credential Offer created successfully.");
                        }
                );
    }

}
