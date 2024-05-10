package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.model.CredentialConfiguration;
import es.in2.issuer.domain.model.CredentialDefinition;
import es.in2.issuer.domain.model.CredentialIssuerMetadata;
import es.in2.issuer.domain.service.CredentialIssuerMetadataService;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static es.in2.issuer.domain.util.Constants.*;
import static es.in2.issuer.domain.util.EndpointsConstants.*;
import static es.in2.issuer.domain.util.HttpUtils.ensureUrlHasProtocol;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialIssuerMetadataServiceImpl implements CredentialIssuerMetadataService {

    private final AppConfiguration appConfiguration;

    @Override
    public Mono<CredentialIssuerMetadata> generateOpenIdCredentialIssuer() {
        String credentialIssuerDomain = ensureUrlHasProtocol(appConfiguration.getIssuerExternalDomain());

        CredentialConfiguration learCredentialEmployee = CredentialConfiguration.builder()
                .format(JWT_VC_JSON)
                .cryptographicBindingMethodsSupported(List.of())
                .credentialSigningAlgValuesSupported(List.of())
                .credentialDefinition(CredentialDefinition.builder().type(List.of(LEAR_CREDENTIAL,VERIFIABLE_CREDENTIAL)).build())
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
