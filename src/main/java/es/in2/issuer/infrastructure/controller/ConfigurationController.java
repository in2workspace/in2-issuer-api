package es.in2.issuer.infrastructure.controller;

import com.microsoft.aad.msal4j.IHttpResponse;
import es.in2.issuer.domain.model.entities.Configuration;
import es.in2.issuer.domain.service.AccessTokenService;
import es.in2.issuer.domain.service.ConfigurationService;
import es.in2.issuer.infrastructure.repository.ConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("api/v1/configuration")
@RequiredArgsConstructor
@Slf4j
public class ConfigurationController {
    private final AccessTokenService accessTokenService;
    private final ConfigurationService configurationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ResponseEntity<Void>> saveConfiguration(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
                                                        @RequestBody Map<String, String> settings ){
        return accessTokenService.getOrganizationId(authorizationHeader)
                .doOnNext(orgId -> log.debug("Saving config for organization : {}", orgId))
                .flatMap(orgId -> configurationService.saveConfiguration(orgId, settings))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());

    }

    @GetMapping
    public Mono<ResponseEntity<Map<String, String>>> getConfigurationsByOrganization(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader
    ) {
        return accessTokenService.getOrganizationId(authorizationHeader)
                .flatMap(orgId -> configurationService.getConfigurationMapByOrganization(orgId))
                .map(configMap -> ResponseEntity.ok().body(configMap));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> patchConfigurations(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestBody Map<String, String> updates
    ) {
        return accessTokenService.getOrganizationId(authorizationHeader)
                .flatMap(orgId -> configurationService.updateOrInsertKeys(orgId, updates));
    }


}
