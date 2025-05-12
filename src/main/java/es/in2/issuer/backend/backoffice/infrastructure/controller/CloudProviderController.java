package es.in2.issuer.backend.backoffice.infrastructure.controller;

import es.in2.issuer.backend.backoffice.domain.model.entities.CloudProvider;
import es.in2.issuer.backend.backoffice.domain.service.CloudProviderService;
import es.in2.issuer.backend.shared.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/backoffice/v1/signatures/cloud-providers")
public class CloudProviderController {
    private final CloudProviderService cloudProviderService;

    @Operation(
            summary = "Create a new cloud provider",
            description = "Registers a new cloud signature provider",
            tags = {SwaggerConfig.TAG_PRIVATE}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cloud provider created"),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
    })
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<CloudProvider>> createCloudProvider(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody CloudProvider request) {
        log.debug("Creating new cloud provider: {}", request.getProvider());
        return cloudProviderService.save(request)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @Operation(
            summary = "List all cloud providers",
            description = "Returns all cloud providers currently registered",
            tags = {SwaggerConfig.TAG_PRIVATE}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of cloud providers returned successfully"),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CloudProvider> getAllCloudProviders(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        log.debug("Fetching all cloud providers");
        return cloudProviderService.findAll();
    }

}
