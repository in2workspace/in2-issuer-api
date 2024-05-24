package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.model.dto.CredentialConfiguration;
import es.in2.issuer.domain.model.dto.CredentialDefinition;
import es.in2.issuer.domain.model.dto.CredentialIssuerMetadata;
import es.in2.issuer.domain.service.CredentialIssuerMetadataService;
import es.in2.issuer.infrastructure.config.ApiConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static es.in2.issuer.domain.util.Constants.*;
import static es.in2.issuer.domain.util.EndpointsConstants.*;
import static es.in2.issuer.domain.util.HttpUtils.ensureUrlHasProtocol;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialIssuerMetadataServiceImpl implements CredentialIssuerMetadataService {

    private final ApiConfig apiConfig;

    @Override
    public Mono<CredentialIssuerMetadata> generateOpenIdCredentialIssuer() {
        String credentialIssuerDomain = ensureUrlHasProtocol(apiConfig.getIssuerApiExternalDomain());
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
