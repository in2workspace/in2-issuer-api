package es.in2.issuer.backend.backoffice.infrastructure.controller;

import es.in2.issuer.backend.backoffice.domain.model.entities.CloudProvider;
import es.in2.issuer.backend.backoffice.domain.service.CloudProviderService;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping
public class CloudProviderController {
    private final CloudProviderService cloudProviderService;

    @PostMapping(path = "/ops/v1/signatures/cloud-providers",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<CloudProvider>> createCloudProvider(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody CloudProvider request) {
        log.debug("Creating new cloud provider: {}", request.getProvider());
        return cloudProviderService.save(request)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @GetMapping(path = "/backoffice/v1/signatures/cloud-providers",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<CloudProvider> getAllCloudProviders(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        log.debug("Fetching all cloud providers");
        return cloudProviderService.findAll();
    }

}
