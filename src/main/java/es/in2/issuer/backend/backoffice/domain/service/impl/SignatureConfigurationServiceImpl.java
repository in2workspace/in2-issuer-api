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
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static es.in2.issuer.backend.backoffice.domain.util.Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignatureConfigurationServiceImpl implements SignatureConfigurationService {
    private final VaultService vaultService;
    private final SignatureConfigurationRepository repository;
    private final CloudProviderService cloudProviderService;
    private final SignatureConfigurationAuditService signatureConfigurationAuditService;

    @Override
    public Mono<SignatureConfiguration> saveSignatureConfig(
            CompleteSignatureConfiguration config,
            String organizationIdentifier) {

        validateBasicConfig(config);

        UUID generatedId = UUID.randomUUID();
        String secretRelativePath = organizationIdentifier + "/" + generatedId;
        SignatureConfiguration baseConfig = buildBaseConfig(config, organizationIdentifier, generatedId);

        return switch (config.signatureMode()) {
            case CLOUD   -> handleCloudMode(config, baseConfig, secretRelativePath);
            case SERVER  -> handleServerMode(config, baseConfig);
            case LOCAL   -> repository.save(baseConfig);
        };
    }

    private void validateBasicConfig(CompleteSignatureConfiguration config) {
        if (config.signatureMode() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "signatureMode must not be null");
        }
        if (Boolean.FALSE.equals(config.enableRemoteSignature()) && config.signatureMode() != SignatureMode.LOCAL) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Remote signature must be enabled for SERVER or CLOUD modes"
            );
        }
    }

    private SignatureConfiguration buildBaseConfig(
            CompleteSignatureConfiguration config,
            String orgId,
            UUID id) {

        return SignatureConfiguration.builder()
                .id(id)
                .organizationIdentifier(orgId)
                .enableRemoteSignature(config.enableRemoteSignature())
                .signatureMode(config.signatureMode())
                .newTransaction(true)
                .build();
    }

    //--- CLOUD mode ---
    private Mono<SignatureConfiguration> handleCloudMode(
            CompleteSignatureConfiguration config,
            SignatureConfiguration signatureConfigData,
            String secretRelativePath) {

        // Validate required fields for CLOUD mode
        if ( Stream.of(
                config.clientId(),
                config.clientSecret(),
                config.credentialId(),
                config.credentialName(),
                config.credentialPassword(),
                config.cloudProviderId()
        ).anyMatch(Objects::isNull) ) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Secret (TOTP) is required by the provider"
            ));
        }

        signatureConfigData.setClientId(config.clientId());
        signatureConfigData.setCredentialId(config.credentialId());
        signatureConfigData.setCredentialName(config.credentialName());
        signatureConfigData.setCloudProviderId(config.cloudProviderId());
        signatureConfigData.setSecretRelativePath(secretRelativePath);

        // Flow to check if TOTP is required and save secrets
        return cloudProviderService.requiresTOTP(config.cloudProviderId())
                .switchIfEmpty(Mono.error(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Cloud provider not found")))
                .flatMap(requiresTOTP -> {
                    if (Boolean.TRUE.equals(requiresTOTP) && config.secret() == null) {
                        return Mono.error(new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Secret (TOTP) is required by the provider"
                        ));
                    }
                    Map<String, String> secrets = new HashMap<>();
                    secrets.put("clientSecret", config.clientSecret());
                    secrets.put(CREDENTIAL_PASSWORD, config.credentialPassword());
                    if (Boolean.TRUE.equals(requiresTOTP)) {
                        secrets.put(SECRET, config.secret());
                    }
                    return vaultService
                            .saveSecrets(secretRelativePath, secrets)
                            .then(repository.save(signatureConfigData));
                });
    }

    //--- SERVER mode ---
    private Mono<SignatureConfiguration> handleServerMode(
            CompleteSignatureConfiguration config,
            SignatureConfiguration signatureConfigData) {

        if (config.credentialId() == null || config.credentialName() == null) {
            return Mono.error(new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Missing required fields for SERVER mode"
            ));
        }
        signatureConfigData.setCredentialId(config.credentialId());
        signatureConfigData.setCredentialName(config.credentialName());
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
    public Mono<Void> updateSignatureConfiguration(
            String id,
            String organizationId,
            CompleteSignatureConfiguration newConfig,
            String rationale,
            String userEmail) {

        UUID configId = UUID.fromString(id);

        return getCompleteConfigurationById(id, organizationId)
                .flatMap(oldConfig ->
                        findExistingConfig(configId)
                                .flatMap(existing ->
                                        patchSecretsIfNeeded(existing, newConfig)
                                                .then(saveConfig(existing))
                                                .then(saveAudit(oldConfig, newConfig, rationale, userEmail))
                                )
                );
    }

    //--- Look for existing configuration ---
    private Mono<SignatureConfiguration> findExistingConfig(UUID configId) {
        return repository.findById(configId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Configuration not found")));
    }

    //--- If needed, update secrets in Vault ---
    private Mono<Void> patchSecretsIfNeeded(
            SignatureConfiguration existing,
            CompleteSignatureConfiguration newConfig) {

        Map<String, String> partialSecrets = new HashMap<>();
        if (newConfig.clientSecret()       != null) partialSecrets.put(CLIENT_SECRET,      newConfig.clientSecret());
        if (newConfig.credentialPassword() != null) partialSecrets.put(CREDENTIAL_PASSWORD, newConfig.credentialPassword());
        if (newConfig.secret()             != null) partialSecrets.put(SECRET,             newConfig.secret());

        return partialSecrets.isEmpty()
                ? Mono.empty()
                : vaultService.patchSecrets(existing.getSecretRelativePath(), partialSecrets);
    }

    //--- Save the updated configuration ---
    private Mono<SignatureConfiguration> saveConfig(SignatureConfiguration existing) {
        return repository.save(existing);
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
                        toStringOrNull(secretsMap.get(CLIENT_SECRET)),
                        toStringOrNull(secretsMap.get(CREDENTIAL_PASSWORD)),
                        toStringOrNull(secretsMap.get(SECRET))
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
