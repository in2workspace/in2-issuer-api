package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.model.AppNonceValidationResponse;
import es.in2.issuer.domain.model.NonceResponse;
import es.in2.issuer.domain.service.NonceManagementService;
import es.in2.issuer.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/nonce")
@RequiredArgsConstructor
public class NonceManagementController {

    private final NonceManagementService nonceManagementService;
    @Operation(
            summary = "Generate and Pair Nonce with Access Token",
            description = "This endpoint receives an access token and generates a corresponding nonce (usually from the authorization server), pairing it with the token for secure authentication and authorization processes within a verifiable credential issuance context",
            tags = {SwaggerConfig.TAG_PRIVATE}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returns the nonce value and its expiration details"
                    )
            }
    )
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<NonceResponse> saveAccessTokenAndNonce(@RequestBody AppNonceValidationResponse appNonceValidationResponse) {
        return nonceManagementService.saveAccessTokenAndNonce(appNonceValidationResponse);
    }
}
