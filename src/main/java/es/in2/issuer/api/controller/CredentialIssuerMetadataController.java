package es.in2.issuer.api.controller;

import es.in2.issuer.api.config.swagger.SwaggerConfig;
import es.in2.issuer.api.model.dto.GenericResponseError;
import es.in2.issuer.api.service.CredentialIssuerMetadataService;
import es.in2.issuer.api.util.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/.well-known/openid-credential-issuer")
@RequiredArgsConstructor
public class CredentialIssuerMetadataController {

    private final CredentialIssuerMetadataService credentialIssuerMetadataService;

    @Operation(
            summary = "Retrieve OpenID Connect Credential Issuer Metadata",
            description = "Provides access to OpenID Connect (OIDC) Credential Issuer Metadata, which includes essential information about the OIDC credential issuer. The metadata offers details such as the issuer's URL, supported authentication methods, and other relevant information. Clients can use this metadata to configure their OIDC integration with the credential issuer. The response is in JSON format",
            tags = SwaggerConfig.TAG_PRIVATE
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returns OpenID Connect (OIDC) Credential Issuer Metadata",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "This response is returned when an unexpected server error occurs. It includes an error message if one is available.",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GenericResponseError.class))
                    )
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<String> getOpenIdCredentialIssuer() {
        try {
            return Mono.just(Utils.toJsonString(credentialIssuerMetadataService.generateOpenIdCredentialIssuer()));
        } catch (Exception e) {
            return Mono.error(new RuntimeException(e));
        }
    }

}
