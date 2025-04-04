package es.in2.issuer.oidc4vci.domain.service.impl;

import es.in2.issuer.oidc4vci.domain.model.dto.AuthorizationServerMetadata;
import es.in2.issuer.oidc4vci.domain.service.AuthorizationServerMetadataService;
import es.in2.issuer.shared.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationServerMetadataServiceImpl implements AuthorizationServerMetadataService {

    private static final String TOKEN_ENDPOINT_PATTERN = "%s/token";
    private final AppConfig appConfig;

    @Override
    public Mono<AuthorizationServerMetadata> generateOpenIdAuthorizationServerMetadata() {
        return Mono.just(new AuthorizationServerMetadata(
                String.format(TOKEN_ENDPOINT_PATTERN, appConfig.getIssuerApiExternalDomain())));
    }
}