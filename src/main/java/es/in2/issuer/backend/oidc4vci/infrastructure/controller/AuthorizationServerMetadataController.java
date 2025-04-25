package es.in2.issuer.backend.oidc4vci.infrastructure.controller;

import es.in2.issuer.backend.oidc4vci.domain.model.dto.AuthorizationServerMetadata;
import es.in2.issuer.backend.oidc4vci.domain.service.AuthorizationServerMetadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static es.in2.issuer.backend.shared.domain.util.Constants.ENGLISH;

@RestController
@RequestMapping("/.well-known/openid-configuration")
@RequiredArgsConstructor
public class AuthorizationServerMetadataController {

    private final AuthorizationServerMetadataService authorizationServerMetadataService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<AuthorizationServerMetadata> getCredentialIssuerMetadata(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(HttpHeaders.CONTENT_LANGUAGE, ENGLISH);
        return authorizationServerMetadataService.generateOpenIdAuthorizationServerMetadata();
    }
}
