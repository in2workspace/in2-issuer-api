package es.in2.issuer.infrastructure.controller;
import es.in2.issuer.domain.model.AuthorizationServerMetadata;
import es.in2.issuer.domain.model.GlobalErrorMessage;
import es.in2.issuer.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/.well-known/openid-configuration")
@RequiredArgsConstructor
public class AuthorizationServerMetadataController {
    // this controller is for mocking purposes until the implementation of a proxy for the authorization server custom endpoint
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
                            responseCode = "500", description = "This response is returned when an unexpected server error occurs. It includes an error message if one is available.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = GlobalErrorMessage.class))
                    )
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public AuthorizationServerMetadata getAuthorizationServerMetadata() {
        return AuthorizationServerMetadata.builder().tokenEndpoint("http://localhost:8088/realms/EAAProvider/verifiable-credential/did:key:z6MkqmaCT2JqdUtLeKah7tEVfNXtDXtQyj4yxEgV11Y5CqUa/token").build();

    }

}