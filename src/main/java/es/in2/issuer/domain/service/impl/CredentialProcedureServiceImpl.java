package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.CredentialTypeUnsupportedException;
import es.in2.issuer.domain.exception.NoCredentialFoundException;
import es.in2.issuer.domain.model.dto.CredentialDetails;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.dto.CredentialProcedures;
import es.in2.issuer.domain.model.dto.ProcedureBasicInfo;
import es.in2.issuer.domain.model.entities.CredentialProcedure;
import es.in2.issuer.domain.model.enums.CredentialStatus;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.infrastructure.repository.CredentialProcedureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class CredentialProcedureServiceImpl implements CredentialProcedureService {

    private final CredentialProcedureRepository credentialProcedureRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> createCredentialProcedure(CredentialProcedureCreationRequest credentialProcedureCreationRequest) {
        CredentialProcedure credentialProcedure = CredentialProcedure.builder()
                .credentialId(UUID.fromString(credentialProcedureCreationRequest.credentialId()))
                .credentialStatus(CredentialStatus.WITHDRAWN)
                .credentialDecoded(credentialProcedureCreationRequest.credentialDecoded())
                .organizationIdentifier(credentialProcedureCreationRequest.organizationIdentifier())
                .updatedAt(new Timestamp(Instant.now().toEpochMilli()))
                .build();
        return credentialProcedureRepository.save(credentialProcedure)
                .map(savedCredentialProcedure -> savedCredentialProcedure.getProcedureId().toString())
                .doOnError(e -> log.error("Error saving credential procedure", e));
    }

    @Override
    public Mono<String> getCredentialTypeByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        return extractCredentialType(credential);
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }
                });
    }

    @Override
    public Mono<Void> updateDecodedCredentialByProcedureId(String procedureId, String credential, String format) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    credentialProcedure.setCredentialDecoded(credential);
                    credentialProcedure.setCredentialStatus(CredentialStatus.ISSUED);
                    credentialProcedure.setCredentialFormat(format);
                    credentialProcedure.setUpdatedAt(new Timestamp(Instant.now().toEpochMilli()));
                    return credentialProcedureRepository.save(credentialProcedure)
                            .doOnSuccess(result -> log.info("Updated credential"))
                            .then();
                });
    }

    @Override
    public Mono<String> getDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> Mono.just(credentialProcedure.getCredentialDecoded()));
    }

    @Override
    public Mono<String> getEncodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> Mono.just(credentialProcedure.getCredentialEncoded()));
    }

    @Override
    public Mono<String> getCredentialStatusByProcedureId(String procedureId) {
        return credentialProcedureRepository.findCredentialStatusByProcedureId(UUID.fromString(procedureId));
    }

    @Override
    public Mono<String> getCredentialSubjectEmailFromDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        return extractCredentialDetail(credential, "email");
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException());
                    }
                });
    }

    @Override
    public Mono<String> getCredentialSubjectNameFromDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        return extractCredentialDetail(credential, "name");
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException());
                    }
                });
    }

    @Override
    public Mono<String> getSignerEmailFromDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        return extractCredentialDetail(credential, "signerEmail");
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException());
                    }
                });
    }

    @Override
    public Flux<String> getAllIssuedCredentialByOrganizationIdentifier(String organizationIdentifier) {
        return credentialProcedureRepository.findByCredentialStatusAndOrganizationIdentifier(CredentialStatus.ISSUED, organizationIdentifier)
                .map(CredentialProcedure::getCredentialDecoded);
    }

    @Override
    public Mono<CredentialDetails> getProcedureDetailByProcedureIdAndOrganizationId(String organizationIdentifier, String procedureId) {
        return credentialProcedureRepository.findByProcedureIdAndOrganizationIdentifier(UUID.fromString(procedureId), organizationIdentifier)
                .switchIfEmpty(Mono.error(new NoCredentialFoundException("No credential found for procedureId: " + procedureId)))
                .flatMap(credentialProcedure -> {
                    try {
                        return Mono.just(CredentialDetails.builder()
                                .procedureId(credentialProcedure.getProcedureId())
                                .credentialStatus(String.valueOf(credentialProcedure.getCredentialStatus()))
                                .credential(objectMapper.readTree(credentialProcedure.getCredentialDecoded()))
                                .build());
                    } catch (JsonProcessingException e) {
                        log.warn(PARSIGN_ERROR_MESSAGE, e);
                        return Mono.error(new JsonParseException(null, PARSIGN_ERROR_MESSAGE));
                    }
                })
                .doOnError(error -> log.error("Could not load credentials, error: {}", error.getMessage()));
    }

    @Override
    public Mono<String> updatedEncodedCredentialByCredentialId(String encodedCredential, String credentialId) {
        return credentialProcedureRepository.findByCredentialId(UUID.fromString(credentialId))
                .flatMap(credentialProcedure -> {
                    credentialProcedure.setCredentialEncoded(encodedCredential);
                    credentialProcedure.setCredentialStatus(CredentialStatus.PEND_DOWNLOAD);
                    return credentialProcedureRepository.save(credentialProcedure)
                            .then(Mono.just(credentialProcedure.getProcedureId().toString()));
                });
    }

    @Override
    public Mono<Void> updateCredentialProcedureCredentialStatusToValidByProcedureId(String procedureId) {
        return credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    credentialProcedure.setCredentialStatus(CredentialStatus.VALID);
                    return credentialProcedureRepository.save(credentialProcedure)
                            .doOnSuccess(result -> log.info("Updated credential"))
                            .then();
                });
    }

    @Override
    public Mono<CredentialProcedures> getAllProceduresBasicInfoByOrganizationId(String organizationIdentifier) {
        return credentialProcedureRepository.findAllByOrganizationIdentifier(organizationIdentifier)
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        return extractCredentialDetail(credential, "fullName")
                                .map(subjectFullName -> ProcedureBasicInfo.builder()
                                        .procedureId(credentialProcedure.getProcedureId())
                                        .fullName(subjectFullName)
                                        .status(String.valueOf(credentialProcedure.getCredentialStatus()))
                                        .updated(credentialProcedure.getUpdatedAt())
                                        .build());
                    } catch (JsonProcessingException e) {
                        log.warn("Error processing json", e);
                        return Mono.error(new JsonParseException(null, PARSIGN_ERROR_MESSAGE));
                    }
                })
                .map(procedureBasicInfo -> CredentialProcedures.CredentialProcedure.builder()
                        .credentialProcedure(procedureBasicInfo)
                        .build())
                .collectList()
                .map(CredentialProcedures::new);
    }

    private Mono<String> extractCredentialType(JsonNode credential) {
        JsonNode typeNode = credential.get("vc").get("type");
        if (typeNode != null && typeNode.isArray()) {
            for (JsonNode type : typeNode) {
                if (!type.asText().equals("VerifiableCredential") && !type.asText().equals("VerifiableAttestation")) {
                    return Mono.just(type.asText());
                }
            }
            return Mono.empty();
        } else {
            return Mono.error(new RuntimeException("The credential type is missing"));
        }
    }

    private Mono<String> extractCredentialDetail(JsonNode credential, String detailType) {
        JsonNode types = credential.get("vc").get("type");
        if (types != null && types.isArray()) {
            for (JsonNode type : types) {
                if (type.asText().equals(LEAR_CREDENTIAL_EMPLOYEE)) {
                    return getDetailBasedOnType(credential, detailType, true);
                } else if (type.asText().equals(VERIFIABLE_CERTIFICATION)) {
                    return getDetailBasedOnType(credential, detailType, false);
                }
            }
        }
        return Mono.error(new CredentialTypeUnsupportedException(CREDENTIAL_TYPE_UNSUPPORTED));
    }

    private Mono<String> getDetailBasedOnType(JsonNode credential, String detailType, boolean isLEARCredential) {
        return switch (detailType) {
            case "email" ->
                    isLEARCredential ? Mono.just(credential.get("vc").get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATEE).get("email").asText())
                            : Mono.just(credential.get("vc").get(CREDENTIAL_SUBJECT).get("company").get("email").asText());
            case "name" ->
                    isLEARCredential ? Mono.just(credential.get("vc").get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATEE).get("first_name").asText())
                            : Mono.just(credential.get("vc").get(CREDENTIAL_SUBJECT).get("company").get("commonName").asText());
            case "signerEmail" ->
                    isLEARCredential ? Mono.just(credential.get("vc").get(CREDENTIAL_SUBJECT).get(MANDATE).get("signer").get("emailAddress").asText())
                            : Mono.just(credential.get("vc").get("signer").get("emailAddress").asText());
            case "fullName" ->
                    isLEARCredential ? Mono.just(credential.get("vc").get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATEE).get("first_name").asText()
                            + " " + credential.get("vc").get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATEE).get("last_name").asText())
                            : Mono.just(credential.get("vc").get(CREDENTIAL_SUBJECT).get("product").get("productName").asText());
            default -> Mono.error(new IllegalArgumentException("Unknown detail type: " + detailType));
        };
    }
}
