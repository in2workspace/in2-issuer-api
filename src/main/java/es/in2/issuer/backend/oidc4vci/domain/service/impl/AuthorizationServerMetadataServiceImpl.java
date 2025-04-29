package es.in2.issuer.backend.oidc4vci.domain.service.impl;

import es.in2.issuer.backend.oidc4vci.domain.model.AuthorizationServerMetadata;
import es.in2.issuer.backend.oidc4vci.domain.service.AuthorizationServerMetadataService;
import es.in2.issuer.backend.shared.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Set;

import static es.in2.issuer.backend.shared.domain.util.EndpointsConstants.OAUTH_TOKEN_PATH;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationServerMetadataServiceImpl implements AuthorizationServerMetadataService {

    private final AppConfig appConfig;

    @Override
    public Mono<AuthorizationServerMetadata> buildAuthorizationServerMetadata(String processId) {
        String issuerUrl = appConfig.getIssuerBackendUrl();
        return Mono.just(
                AuthorizationServerMetadata.builder()
                        .issuer(issuerUrl)
                        .tokenEndpoint(issuerUrl + OAUTH_TOKEN_PATH)
                        .responseTypesSupported(Set.of("token"))
                        .preAuthorizedGrantAnonymousAccessSupported(true)
                        .build()
        );
    }
}