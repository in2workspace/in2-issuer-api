package es.in2.issuer.backend.domain.service.impl;

import es.in2.issuer.backend.domain.model.dto.CredentialConfiguration;
import es.in2.issuer.backend.domain.model.dto.CredentialDefinition;
import es.in2.issuer.backend.domain.model.dto.CredentialIssuerMetadata;
import es.in2.issuer.backend.domain.service.CredentialIssuerMetadataService;
import es.in2.issuer.backend.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static es.in2.issuer.backend.domain.util.Constants.*;
import static es.in2.issuer.backend.domain.util.EndpointsConstants.*;
import static es.in2.issuer.backend.domain.util.HttpUtils.ensureUrlHasProtocol;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialIssuerMetadataServiceImpl implements CredentialIssuerMetadataService {

    private final AppConfig appConfig;

    @Override
    public Mono<CredentialIssuerMetadata> generateOpenIdCredentialIssuer() {
        String credentialIssuerDomain = ensureUrlHasProtocol(appConfig.getIssuerApiExternalDomain());
        CredentialConfiguration learCredentialEmployee = CredentialConfiguration.builder()
                .format(JWT_VC_JSON)
                .cryptographicBindingMethodsSupported(List.of())
                .credentialSigningAlgValuesSupported(List.of())
                .credentialDefinition(CredentialDefinition.builder().type(List.of(LEAR_CREDENTIAL, VERIFIABLE_CREDENTIAL)).build())
                .build();
        return Mono.just(
                CredentialIssuerMetadata.builder()
                        .credentialIssuer(credentialIssuerDomain)
                        .credentialEndpoint(credentialIssuerDomain + CREDENTIAL)
                        .batchCredentialEndpoint(credentialIssuerDomain + CREDENTIAL_BATCH)
                        .deferredCredentialEndpoint(credentialIssuerDomain + CREDENTIAL_DEFERRED)
                        .credentialConfigurationsSupported(Map.of(LEAR_CREDENTIAL_EMPLOYEE, learCredentialEmployee))
                        .build()
        );
    }

}
