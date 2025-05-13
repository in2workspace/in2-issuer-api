package es.in2.issuer.backend.backoffice.domain.service.impl;

import es.in2.issuer.backend.backoffice.domain.exception.NoSuchEntityException;
import es.in2.issuer.backend.backoffice.domain.exception.OrganizationIdentifierMismatchException;
import es.in2.issuer.backend.backoffice.domain.model.dtos.CompleteSignatureConfiguration;
import es.in2.issuer.backend.backoffice.domain.model.dtos.SignatureConfigWithProviderName;
import es.in2.issuer.backend.backoffice.domain.model.dtos.SignatureConfigurationResponse;
import es.in2.issuer.backend.backoffice.domain.model.dtos.SignatureVaultSecret;
import es.in2.issuer.backend.backoffice.domain.model.entities.SignatureConfiguration;
import es.in2.issuer.backend.backoffice.domain.model.enums.SignatureMode;
import es.in2.issuer.backend.backoffice.domain.repository.SignatureConfigurationRepository;
import es.in2.issuer.backend.backoffice.domain.service.CloudProviderService;
import es.in2.issuer.backend.backoffice.domain.service.SignatureConfigurationAuditService;
import es.in2.issuer.backend.backoffice.domain.service.SignatureConfigurationService;
import es.in2.issuer.backend.backoffice.domain.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignatureConfigurationServiceImpl implements SignatureConfigurationService {
    private final VaultService vaultService;
    private final SignatureConfigurationRepository repository;
    private final CloudProviderService cloudProviderService;
    private final SignatureConfigurationAuditService signatureConfigurationAuditService;

    @Override
    public Mono<SignatureConfiguration> saveSignatureConfig(CompleteSignatureConfiguration config, String organizationIdentifier) {

        if (config.signatureMode() == null) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "signatureMode must not be null"));
        }
        if (!config.enableRemoteSignature() && config.signatureMode() != SignatureMode.LOCAL) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Remote signature must be enabled for SERVER or CLOUD modes"));
        }

        UUID generatedId = UUID.randomUUID();
        String secretRelativePath = organizationIdentifier + "/" + generatedId;

        SignatureConfiguration signatureConfigData = SignatureConfiguration.builder()
                .id(generatedId)
                .organizationIdentifier(organizationIdentifier)
                .enableRemoteSignature(config.enableRemoteSignature())
                .signatureMode(config.signatureMode())
                .newTransaction(true)
                .build();

        // Si es CLOUD
        if (config.signatureMode() == SignatureMode.CLOUD) {
            if (config.clientId() == null || config.clientSecret() == null || config.credentialId() == null ||
                    config.credentialName() == null || config.credentialPassword() == null || config.cloudProviderId() == null) {
                return Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Secret (TOTP) is required by the provider"));
            }

            signatureConfigData.setClientId(config.clientId());
            signatureConfigData.setCredentialId(config.credentialId());
            signatureConfigData.setCredentialName(config.credentialName());
            signatureConfigData.setCloudProviderId(config.cloudProviderId());
            signatureConfigData.setSecretRelativePath(secretRelativePath);

            return cloudProviderService.requiresTOTP(config.cloudProviderId())
                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cloud provider not found")))
                    .flatMap(requiresTOTP -> {
                        if (requiresTOTP && config.secret() == null) {
                            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Secret (TOTP) is required by the provider"));
                        }

                        Map<String, String> secretsToSave = new HashMap<>();
                        secretsToSave.put("clientSecret", config.clientSecret());
                        secretsToSave.put("credentialPassword", config.credentialPassword());
                        if (requiresTOTP) {
                            secretsToSave.put("secret", config.secret());
                        }

                        return vaultService.saveSecrets(secretRelativePath, secretsToSave)
                                .then(repository.save(signatureConfigData));
                    });
        }

        // Si es SERVER
        if (config.signatureMode() == SignatureMode.SERVER) {
            if (config.credentialId() == null || config.credentialName() == null) {
                return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing required fields for SERVER mode"));
            }
            signatureConfigData.setCredentialId(config.credentialId());
            signatureConfigData.setCredentialName(config.credentialName());
            return repository.save(signatureConfigData);
        }

        // Si es LOCAL
        return repository.save(signatureConfigData);
    }

    @Override
    public Mono<Map<String, Object>> getSignatureCredentials(String secretRelativePath) {
        return vaultService.getSecrets(secretRelativePath);
    }

    @Override
    public Flux<SignatureConfigWithProviderName> findAllByOrganizationIdentifierAndMode(String organizationIdentifier, SignatureMode signatureMode) {
        Flux<SignatureConfiguration> configs = (signatureMode != null)
                ? repository.findAllByOrganizationIdentifierAndSignatureMode(organizationIdentifier, signatureMode)
                : repository.findAllByOrganizationIdentifier(organizationIdentifier);

        return configs.flatMap(config -> {
            if (config.getCloudProviderId() == null) {
                return Mono.just(mapToWithProviderName(config, null));
            }

            return cloudProviderService.findById(config.getCloudProviderId())
                    .map(provider -> mapToWithProviderName(config, provider.getProvider()));
        });
    }

    @Override
    public Mono<SignatureConfigurationResponse> getCompleteConfigurationById(String id, String organizationId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format: {}", id, e);
            return Mono.error(new IllegalArgumentException("Invalid UUID format: " + id));
        }

        return repository.findById(uuid)
                .switchIfEmpty(Mono.error(new NoSuchEntityException("Signature configuration not found with ID: " + id)))
                .flatMap(config -> {
                    if (!organizationId.equals(config.getOrganizationIdentifier())) {
                        return Mono.error(new OrganizationIdentifierMismatchException("The organization identifier does not match the organization identifier of the configuration"));
                    }
                    return Mono.just(mapToSignatureConfigurationResponse(config));
                });
    }

    @Override
    public Mono<Void> updateSignatureConfiguration(String id, String organizationId, CompleteSignatureConfiguration newConfig, String rationale, String userEmail) {
        UUID configId = UUID.fromString(id);
        return getCompleteConfigurationById(id, organizationId)
                .flatMap(oldConfig ->
                        repository.findById(configId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Configuration not found")))
                                .flatMap(existing -> {
                                    Mono<Void> secretUpdate = Mono.empty();
                                    if (newConfig.clientSecret() != null || newConfig.credentialPassword() != null || newConfig.secret() != null) {
                                        Map<String, String> partialSecrets = new java.util.HashMap<>();
                                        if (newConfig.clientSecret() != null) partialSecrets.put("clientSecret", newConfig.clientSecret());
                                        if (newConfig.credentialPassword() != null) partialSecrets.put("credentialPassword", newConfig.credentialPassword());
                                        if (newConfig.secret() != null) partialSecrets.put("secret", newConfig.secret());

                                        secretUpdate = vaultService.patchSecrets(existing.getSecretRelativePath(), partialSecrets);
                                    }
                                    return secretUpdate
                                            .then(repository.save(existing))
                                            .then(saveAudit(oldConfig, newConfig, rationale, userEmail));
                                })
                );
    }

    private Mono<Void> saveAudit(SignatureConfigurationResponse oldConfig, CompleteSignatureConfiguration newConfig, String rationale, String userEmail) {
        return signatureConfigurationAuditService.saveAudit(oldConfig, newConfig, rationale, userEmail);
    }

    private SignatureConfigWithProviderName mapToWithProviderName(SignatureConfiguration config, String providerName) {
        return new SignatureConfigWithProviderName(
                config.getId(),
                config.getOrganizationIdentifier(),
                config.isEnableRemoteSignature(),
                config.getSignatureMode(),
                providerName,
                config.getClientId(),
                config.getCredentialId(),
                config.getCredentialName()
        );
    }


    @Override
    public Mono<Void> deleteSignatureConfiguration(String id, String organizationId, String rationale, String userEmail) {

        UUID uuid = UUID.fromString(id);

        return getCompleteConfigurationById(id, organizationId)
                .flatMap(oldConfig -> repository.findById(uuid)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Configuration not found")))
                        .flatMap(existing -> vaultService.deleteSecret(existing.getSecretRelativePath())
                                .then(repository.deleteById(uuid))
                                .then(signatureConfigurationAuditService.saveDeletionAudit(oldConfig,  rationale, userEmail))
                        )
                );
    }

    private Mono<SignatureVaultSecret> getSecretsFromVault(String secretRelativePath) {
        return vaultService.getSecrets(secretRelativePath)
                .map(secretsMap -> new SignatureVaultSecret(
                        toStringOrNull(secretsMap.get("clientSecret")),
                        toStringOrNull(secretsMap.get("credentialPassword")),
                        toStringOrNull(secretsMap.get("secret"))
                ));
    }

    private String toStringOrNull(Object value) {
        return value != null ? value.toString() : null;
    }

    private SignatureConfigurationResponse mapToSignatureConfigurationResponse(SignatureConfiguration config) {
        return new SignatureConfigurationResponse(
                config.getId(),
                config.getOrganizationIdentifier(),
                config.isEnableRemoteSignature(),
                config.getSignatureMode(),
                config.getCloudProviderId(),
                config.getClientId(),
                config.getCredentialId(),
                config.getCredentialName()
        );
    }

}
