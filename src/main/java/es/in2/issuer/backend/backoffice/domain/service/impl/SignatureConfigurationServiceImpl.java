package es.in2.issuer.backend.backoffice.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.backend.backoffice.domain.exception.InvalidSignatureConfigurationException;
import es.in2.issuer.backend.backoffice.domain.exception.MissingRequiredDataException;
import es.in2.issuer.backend.backoffice.domain.exception.NoSuchEntityException;
import es.in2.issuer.backend.backoffice.domain.exception.OrganizationIdentifierMismatchException;
import es.in2.issuer.backend.backoffice.domain.model.dtos.CompleteSignatureConfiguration;
import es.in2.issuer.backend.backoffice.domain.model.dtos.SignatureConfigWithProviderName;
import es.in2.issuer.backend.backoffice.domain.model.dtos.SignatureConfigurationResponse;
import es.in2.issuer.backend.backoffice.domain.model.entities.SignatureConfiguration;
import es.in2.issuer.backend.backoffice.domain.model.enums.SignatureMode;
import es.in2.issuer.backend.backoffice.domain.repository.SignatureConfigurationRepository;
import es.in2.issuer.backend.backoffice.domain.service.CloudProviderService;
import es.in2.issuer.backend.backoffice.domain.service.SignatureConfigurationAuditService;
import es.in2.issuer.backend.backoffice.domain.service.SignatureConfigurationService;
import es.in2.issuer.backend.backoffice.domain.service.VaultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
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
    private final ObjectMapper objectMapper;

    @Override
    public Mono<SignatureConfiguration> saveSignatureConfig(
            CompleteSignatureConfiguration config,
            String organizationIdentifier) {

        validateBasicConfig(config);

        UUID generatedId = UUID.randomUUID();
        String secretRelativePath = organizationIdentifier + SLASH + generatedId;
        SignatureConfiguration baseConfig = buildBaseConfig(config, organizationIdentifier, generatedId);

        return switch (config.signatureMode()) {
            case CLOUD   -> handleCloudMode(config, baseConfig, secretRelativePath);
            case SERVER  -> handleServerMode(config, baseConfig);
            case LOCAL   -> repository.save(baseConfig);
        };
    }

    private void validateBasicConfig(CompleteSignatureConfiguration config) {
        if (config.signatureMode() == null) {
            throw new MissingRequiredDataException("SignatureMode must not be null");
        }
        if (Boolean.FALSE.equals(config.enableRemoteSignature()) && config.signatureMode() != SignatureMode.LOCAL) {
            throw new InvalidSignatureConfigurationException(
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
            return Mono.error(new InvalidSignatureConfigurationException(
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
                .switchIfEmpty(Mono.error(new InvalidSignatureConfigurationException(
                        "Cloud provider not found")))
                .flatMap(requiresTOTP -> {
                    if (Boolean.TRUE.equals(requiresTOTP) && config.secret() == null) {
                        return Mono.error(new InvalidSignatureConfigurationException(
                                "Secret (TOTP) is required by the provider"
                        ));
                    }
                    Map<String, String> secrets = new HashMap<>();
                    secrets.put(CLIENT_SECRET, config.clientSecret());
                    secrets.put(CREDENTIAL_PASSWORD, config.credentialPassword());
                    if (Boolean.TRUE.equals(requiresTOTP)) {
                        secrets.put(SECRET, config.secret());
                    }

                    return vaultService
                            .saveSecrets(secretRelativePath, secrets)
                            .then(Mono.fromSupplier(() -> {
                                // Calculate hashes
                                Map<String, String> hashed = secrets.entrySet().stream()
                                        .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                e -> hashS256(e.getValue())
                                        ));
                                try {
                                    String json = objectMapper.writeValueAsString(hashed);
                                    signatureConfigData.setVaultHashedSecretValues(json);
                                } catch (JsonProcessingException ex) {
                                    throw new IllegalStateException("Error serializing hashes", ex);
                                }
                                return signatureConfigData;
                            }))
                            .flatMap(repository::save);
                });
    }

    //--- SERVER mode ---
    private Mono<SignatureConfiguration> handleServerMode(
            CompleteSignatureConfiguration config,
            SignatureConfiguration signatureConfigData) {

        if (config.credentialId() == null || config.credentialName() == null) {
            return Mono.error(new InvalidSignatureConfigurationException(
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
                // use oldConfig for auditing
                .flatMap(oldConfig ->
                        // load the existing entity
                        repository.findById(configId)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Configuration not found")))
                                .flatMap(existing ->
                                        // 1) patch secrets + update hashes
                                        patchSecretsIfNeeded(existing, newConfig)
                                                // 2) apply the rest of the DTO fields
                                                .doOnNext(updatedEntity -> applyDtoToEntity(updatedEntity, newConfig))
                                                // 3) save the fully updated entity
                                                .flatMap(repository::save)
                                )
                                // 4) record the audit entry
                                .flatMap(saved -> signatureConfigurationAuditService
                                        .saveAudit(oldConfig, newConfig, rationale, userEmail)
                                )
                )
                .then();  // return Mono<Void>
    }

    /**
     * Compares hashes, patches Vault only if there are changes,
     * updates vaultHashedSecretValues on the entity, and returns Mono<entity>.
     */
    private Mono<SignatureConfiguration> patchSecretsIfNeeded(
            SignatureConfiguration existing,
            CompleteSignatureConfiguration newConfig) {

        // 1) Read previous hashes
        Map<String,String> existingHashes = new HashMap<>();
        if (existing.getVaultHashedSecretValues() != null) {
            try {
                existingHashes = objectMapper.readValue(
                        existing.getVaultHashedSecretValues(),
                        new TypeReference<>() {
                        }
                );
            } catch (JsonProcessingException e) {
                return Mono.error(new IllegalStateException("Error parsing previous hashes", e));
            }
        }

        // 2) Compare new values
        Map<String,String> toPatch       = new HashMap<>();
        Map<String,String> updatedHashes = new HashMap<>(existingHashes);

        if (newConfig.clientSecret() != null) {
            String h = hashS256(newConfig.clientSecret());
            if (!h.equals(existingHashes.get(CLIENT_SECRET))) {
                toPatch.put(CLIENT_SECRET, newConfig.clientSecret());
                updatedHashes.put(CLIENT_SECRET, h);
            }
        }
        if (newConfig.credentialPassword() != null) {
            String h = hashS256(newConfig.credentialPassword());
            if (!h.equals(existingHashes.get(CREDENTIAL_PASSWORD))) {
                toPatch.put(CREDENTIAL_PASSWORD, newConfig.credentialPassword());
                updatedHashes.put(CREDENTIAL_PASSWORD, h);
            }
        }
        if (newConfig.secret() != null) {
            String h = hashS256(newConfig.secret());
            if (!h.equals(existingHashes.get(SECRET))) {
                toPatch.put(SECRET, newConfig.secret());
                updatedHashes.put(SECRET, h);
            }
        }

        // 3) Serialize new JSON of hashes into the entity
        try {
            existing.setVaultHashedSecretValues(
                    objectMapper.writeValueAsString(updatedHashes)
            );
        } catch (JsonProcessingException ex) {
            return Mono.error(new IllegalStateException("Failed to serialize updated hashes", ex));
        }

        // 4) If there’s nothing to patch, return the mutated entity
        if (toPatch.isEmpty()) {
            return Mono.just(existing);
        }

        // 5) Patch Vault and then return the entity
        return vaultService
                .patchSecrets(existing.getSecretRelativePath(), toPatch)
                .thenReturn(existing);
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

    private SignatureConfigurationResponse mapToSignatureConfigurationResponse(SignatureConfiguration config) {
        log.info(config.getVaultHashedSecretValues());
        return SignatureConfigurationResponse.builder()
                .id(config.getId())
                .organizationIdentifier(config.getOrganizationIdentifier())
                .enableRemoteSignature(config.isEnableRemoteSignature())
                .signatureMode(config.getSignatureMode())
                .cloudProviderId(config.getCloudProviderId())
                .clientId(config.getClientId())
                .credentialId(config.getCredentialId())
                .credentialName(config.getCredentialName())
                .vaultHashedSecretValues(config.getVaultHashedSecretValues())
                .build();
    }

    private String hashS256(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 Algorithm not supported", e);
        }
    }

    /**
     * Applies all non-null, non-secret fields from the DTO to the entity.
     */
    private void applyDtoToEntity(SignatureConfiguration entity,
                                  CompleteSignatureConfiguration dto) {
        if (dto.enableRemoteSignature() != null) {
            entity.setEnableRemoteSignature(dto.enableRemoteSignature());
        }
        if (dto.signatureMode() != null) {
            entity.setSignatureMode(dto.signatureMode());
        }
        if (dto.cloudProviderId() != null) {
            entity.setCloudProviderId(dto.cloudProviderId());
        }
        if (dto.clientId() != null) {
            entity.setClientId(dto.clientId());
        }
        if (dto.credentialId() != null) {
            entity.setCredentialId(dto.credentialId());
        }
        if (dto.credentialName() != null) {
            entity.setCredentialName(dto.credentialName());
        }
    }
}