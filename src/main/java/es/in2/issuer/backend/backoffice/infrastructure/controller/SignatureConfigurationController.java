package es.in2.issuer.backend.backoffice.infrastructure.controller;

import es.in2.issuer.backend.backoffice.domain.model.dtos.CompleteSignatureConfiguration;
import es.in2.issuer.backend.backoffice.domain.model.dtos.SignatureConfigWithProviderName;
import es.in2.issuer.backend.backoffice.domain.model.dtos.SignatureConfigurationResponse;
import es.in2.issuer.backend.backoffice.domain.model.dtos.UpdateSignatureConfigurationRequest;
import es.in2.issuer.backend.backoffice.domain.model.entities.SignatureConfiguration;
import es.in2.issuer.backend.backoffice.domain.model.enums.SignatureMode;
import es.in2.issuer.backend.backoffice.domain.service.SignatureConfigurationService;
import es.in2.issuer.backend.shared.domain.service.AccessTokenService;
import es.in2.issuer.backend.shared.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/backoffice/v1/signatures/configs")
@RequiredArgsConstructor
public class SignatureConfigurationController {

    private final SignatureConfigurationService signatureConfigurationService;
    private final AccessTokenService accessTokenService;

    @Operation(
            summary = "Create a new signature configuration",
            description = "Creates a new signature configuration, stores the secrets in Vault, and persists the configuration identifier.",
            tags = {SwaggerConfig.TAG_PRIVATE},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Signature configuration created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
            }
    )
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<SignatureConfiguration>> createSignatureConfiguration(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody CompleteSignatureConfiguration config
    ) {

        log.debug("Creating signature configuration for organization: {}", config.organizationIdentifier());
        return accessTokenService.getOrganizationId(authorizationHeader)
                .flatMap(orgId ->signatureConfigurationService.saveSignatureConfig( config, orgId))
                .map(savedConfig -> ResponseEntity.status(HttpStatus.CREATED).body((savedConfig)));

    }

    @Operation(
            summary = "Get signature configurations for a specific organization",
            description = "Returns a list of all signature configurations filtered by the given organization identifier.",
            tags = {SwaggerConfig.TAG_PRIVATE},
            responses = {
                    @ApiResponse(responseCode = "200", description = "All configurations for the specified organization returned"),
                    @ApiResponse(responseCode = "400", description = "Missing or invalid organization identifier", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
            }
    )
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<SignatureConfigWithProviderName> getAllSignatureConfigurations(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(name = "signatureMode", required = false) SignatureMode signatureMode) {
        return accessTokenService.getOrganizationId(authorizationHeader)
                .doOnNext(orgId -> log.debug("Fetching signature configurations for org: {}", orgId))
                .flatMapMany(orgId ->signatureConfigurationService.findAllByOrganizationIdentifierAndMode(orgId, signatureMode));
    }

    @Operation(
            summary = "Get complete signature configuration by ID",
            description = "Returns a complete signature configuration, including secrets stored in Vault, for the given configuration ID.",
            tags = {SwaggerConfig.TAG_PRIVATE},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Complete signature configuration returned successfully"),
                    @ApiResponse(responseCode = "404", description = "Configuration not found", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
            }
    )
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<SignatureConfigurationResponse>> getCompleteConfigurationById(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable String id) {

        log.debug("Fetching complete signature configuration for ID: {}", id);
        return accessTokenService.getOrganizationId(authorizationHeader)
                .flatMap(organizationId -> signatureConfigurationService.getCompleteConfigurationById(id, organizationId)
                        .map(ResponseEntity::ok)
                        .switchIfEmpty(Mono.just(ResponseEntity.notFound().build())));
    }

    @Operation(
            summary = "Update an existing signature configuration",
            description = "Partially updates the signature configuration and secrets in Vault if provided.",
            tags = {SwaggerConfig.TAG_PRIVATE},
            responses = {
                    @ApiResponse(responseCode = "204", description = "Signature configuration updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Configuration not found", content = @Content),
                    @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content)
            }
    )
    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateSignatureConfiguration(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable String id,
            @RequestBody UpdateSignatureConfigurationRequest updateRequest

    ) {
        if (updateRequest.rationale() == null || updateRequest.rationale().isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "'rationale' must be provided"));
        }
        return
                accessTokenService.getOrganizationId(authorizationHeader)
                        .flatMap(organizationId -> accessTokenService.getMandateeEmail(authorizationHeader)
                                .flatMap(userEmail -> signatureConfigurationService.updateSignatureConfiguration(id, organizationId, updateRequest.toCompleteSignatureConfiguration(),updateRequest.rationale(), userEmail)));
    }

    @Operation(
            summary = "Delete an existing signature configuration",
            description = "Deletes the signature configuration and its secrets from Vault if present.",
            tags = {SwaggerConfig.TAG_PRIVATE},
            responses = {
                    @ApiResponse(responseCode = "204", description = "Signature configuration deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Configuration not found", content = @Content)
            }
    )
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteSignatureConfiguration(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable String id,
            @RequestParam() String rationale
    ) {
        log.debug("Deleting signature configuration with ID: {} and rationale: {}", id, rationale);
        return accessTokenService.getOrganizationId(authorizationHeader)
                .flatMap(organizationId -> accessTokenService.getMandateeEmail(authorizationHeader)
                        .flatMap(userEmail ->signatureConfigurationService.deleteSignatureConfiguration( id, organizationId, rationale, userEmail)));

    }

}