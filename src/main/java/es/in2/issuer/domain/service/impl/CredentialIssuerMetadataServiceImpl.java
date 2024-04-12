package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.model.CredentialConfiguration;
import es.in2.issuer.domain.model.CredentialDefinition;
import es.in2.issuer.domain.model.CredentialIssuerMetadata;
import es.in2.issuer.domain.model.VcTemplate;
import es.in2.issuer.domain.service.CredentialIssuerMetadataService;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.iam.util.IamAdapterFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static es.in2.issuer.domain.util.Constants.*;
import static es.in2.issuer.domain.util.HttpUtils.ensureUrlHasProtocol;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialIssuerMetadataServiceImpl implements CredentialIssuerMetadataService {

    private final IamAdapterFactory iamAdapterFactory;
    private final AppConfiguration appConfiguration;

    @Override
    public Mono<CredentialIssuerMetadata> generateOpenIdCredentialIssuer() {
        String credentialIssuerDomain = ensureUrlHasProtocol(appConfiguration.getIssuerDomain());

        CredentialConfiguration learCredentialJwt = CredentialConfiguration.builder()
                .format(JWT_VC_JSON)
                .cryptographicBindingMethodsSupported(List.of())
                .credentialSigningAlgValuesSupported(List.of())
                .credentialDefinition(CredentialDefinition.builder().type(List.of(LEAR_CREDENTIAL,VERIFIABLE_CREDENTIAL)).build())
                .build();
        CredentialConfiguration learCredentialCwt = CredentialConfiguration.builder()
                .format(CWT_VC_JSON)
                .cryptographicBindingMethodsSupported(List.of())
                .credentialSigningAlgValuesSupported(List.of())
                .credentialDefinition(CredentialDefinition.builder().type(List.of(LEAR_CREDENTIAL,VERIFIABLE_CREDENTIAL)).build())
                .build();

        return Mono.just(
                CredentialIssuerMetadata.builder()
                        .credentialIssuer(credentialIssuerDomain)
                        .authorizationServers(List.of(appConfiguration.getIamExternalDomain()))
                        // fixme: Este path debe capturarse de la configuración
                        .credentialEndpoint(credentialIssuerDomain + "/api/vc/credential")
                        .batchCredentialEndpoint(credentialIssuerDomain + "/api/vc/batch_credential")
                        .credentialToken(iamAdapterFactory.getAdapter().getTokenUri()) // Remove for DOME profile
                        .credentialConfigurationsSupported(Map.of(LEAR_CREDENTIAL_JWT, learCredentialJwt, LEAR_CREDENTIAL_CWT, learCredentialCwt))
                        .build()
        );
    }

}
