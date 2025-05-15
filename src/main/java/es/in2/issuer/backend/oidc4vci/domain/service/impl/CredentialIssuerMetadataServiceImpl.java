package es.in2.issuer.backend.oidc4vci.domain.service.impl;

import es.in2.issuer.backend.oidc4vci.domain.model.CredentialIssuerMetadata;
import es.in2.issuer.backend.oidc4vci.domain.service.CredentialIssuerMetadataService;
import es.in2.issuer.backend.shared.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

import static es.in2.issuer.backend.shared.domain.util.Constants.*;
import static es.in2.issuer.backend.shared.domain.util.EndpointsConstants.*;
import static es.in2.issuer.backend.shared.domain.util.HttpUtils.ensureUrlHasProtocol;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialIssuerMetadataServiceImpl implements CredentialIssuerMetadataService {

    private final AppConfig appConfig;
    private static final String ES256_SIGNING_ALG_VALUE = "ES256";

    @Override
    public Mono<CredentialIssuerMetadata> buildCredentialIssuerMetadata(String processId) {
        String credentialIssuerUrl = ensureUrlHasProtocol(appConfig.getIssuerBackendUrl());
        CredentialIssuerMetadata credentialIssuerMetadata = CredentialIssuerMetadata.builder()
                .credentialIssuer(credentialIssuerUrl)
                .issuanceEndpoint(credentialIssuerUrl + VCI_ISSUANCES_PATH)
                .credentialEndpoint(credentialIssuerUrl + OID4VCI_CREDENTIAL_PATH)
                .deferredCredentialEndpoint(credentialIssuerUrl + OID4VCI_DEFERRED_CREDENTIAL_PATH)
                // todo: A NEW ENUM WITH THE 3 CredentialsConfigurationsSupported substituir key
                .credentialConfigurationsSupported(Map.of(
                        LEAR_CREDENTIAL_EMPLOYEE, buildLearCredentialEmployeeCredentialConfiguration(),
                        LEAR_CREDENTIAL_MACHINE, buildLearCredentialMachineCredentialConfiguration(),
                        VERIFIABLE_CERTIFICATION, buildVerifiableCertificationCredentialConfiguration()
                ))
                .build();
        return Mono.just(credentialIssuerMetadata);
    }

    private CredentialIssuerMetadata.CredentialConfiguration buildLearCredentialEmployeeCredentialConfiguration() {
        return CredentialIssuerMetadata.CredentialConfiguration.builder()
                .format(JWT_VC_JSON)
                .scope("lear_credential_employee")
                .cryptographicBindingMethodsSupported(Set.of("did:key"))
                .credentialSigningAlgValuesSupported(Set.of(ES256_SIGNING_ALG_VALUE))
                .credentialDefinition(CredentialIssuerMetadata.CredentialConfiguration.CredentialDefinition.builder()
                        .type(Set.of(VERIFIABLE_CREDENTIAL, LEAR_CREDENTIAL_EMPLOYEE))
                        .build())
                .proofTypesSupported(Map.of("jwt", CredentialIssuerMetadata.CredentialConfiguration.ProofSigninAlgValuesSupported.builder()
                        .proofSigningAlgValuesSupported(Set.of(ES256_SIGNING_ALG_VALUE))
                        .build()))
                .build();
    }

    private CredentialIssuerMetadata.CredentialConfiguration buildLearCredentialMachineCredentialConfiguration() {
        return CredentialIssuerMetadata.CredentialConfiguration.builder()
                .format(JWT_VC_JSON)
                .scope("lear_credential_machine")
                .credentialSigningAlgValuesSupported(Set.of(ES256_SIGNING_ALG_VALUE))
                .credentialDefinition(CredentialIssuerMetadata.CredentialConfiguration.CredentialDefinition.builder()
                        .type(Set.of(VERIFIABLE_CREDENTIAL, LEAR_CREDENTIAL_MACHINE))
                        .build())
                .build();
    }

    private CredentialIssuerMetadata.CredentialConfiguration buildVerifiableCertificationCredentialConfiguration() {
        return CredentialIssuerMetadata.CredentialConfiguration.builder()
                .format(JWT_VC_JSON)
                .scope("verifiable_certification")
                .credentialSigningAlgValuesSupported(Set.of(ES256_SIGNING_ALG_VALUE))
                .credentialDefinition(CredentialIssuerMetadata.CredentialConfiguration.CredentialDefinition.builder()
                        .type(Set.of(VERIFIABLE_CREDENTIAL, VERIFIABLE_CERTIFICATION))
                        .build())
                .build();
    }

}
