package es.in2.issuer.backend.oidc4vci.domain.service.impl;

import es.in2.issuer.backend.oidc4vci.domain.model.dto.AuthorizationServerMetadata;
import es.in2.issuer.backend.oidc4vci.domain.service.AuthorizationServerMetadataService;
import es.in2.issuer.backend.shared.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static es.in2.issuer.backend.shared.domain.util.EndpointsConstants.TOKEN;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationServerMetadataServiceImpl implements AuthorizationServerMetadataService {

    private final AppConfig appConfig;

    @Override
    public Mono<AuthorizationServerMetadata> generateOpenIdAuthorizationServerMetadata() {
        return Mono.just(new AuthorizationServerMetadata(appConfig.getIssuerBackendUrl() + TOKEN));
    }
}