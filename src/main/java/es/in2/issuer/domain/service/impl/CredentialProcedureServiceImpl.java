package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                .credentialStatus(CredentialStatus.DRAFT)
                .credentialDecoded(credentialProcedureCreationRequest.credentialDecoded())
                .organizationIdentifier(credentialProcedureCreationRequest.organizationIdentifier())
                .credentialType(credentialProcedureCreationRequest.credentialType().toString())
                .subject(credentialProcedureCreationRequest.subject())
                .validUntil(credentialProcedureCreationRequest.validUntil())
                .updatedAt(new Timestamp(Instant.now().toEpochMilli()))
                .operationMode(credentialProcedureCreationRequest.operationMode())
                .signatureMode("remote")
                .build();
        return credentialProcedureRepository.save(credentialProcedure)
                .map(savedCredentialProcedure -> savedCredentialProcedure.getProcedureId().toString())
                .doOnError(e -> log.error("Error saving credential procedure", e));
    }

    @Override
    public Mono<String> getCredentialTypeByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(this::getCredentialType);
    }

    private Mono<String> getCredentialType(CredentialProcedure credentialProcedure) {
        try {
            JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
            JsonNode typeNode = credential.has(VC) ? credential.get(VC).get(TYPE) : credential.get(TYPE);
            if (typeNode == null || !typeNode.isArray()) {
                return Mono.error(new RuntimeException("The credential type is missing"));
            }
            String credentialType = extractCredentialType(typeNode);
            return Mono.justOrEmpty(credentialType);
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException(e));
        }
    }

    private String extractCredentialType(JsonNode typeNode) {
        if (typeNode != null && typeNode.isArray()) {
            for (JsonNode type : typeNode) {
                String typeText = type.asText();
                if (!typeText.equals(VERIFIABLE_CREDENTIAL) && !typeText.equals(VERIFIABLE_ATTESTATION)) {
                    return typeText;
                }
            }
        }
        return null;
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
    public Mono<String> getCredentialStatusByProcedureId(String procedureId) {
        log.debug("Getting credential status for procedureId: {}", procedureId);
        return credentialProcedureRepository.findCredentialStatusByProcedureId(UUID.fromString(procedureId));
    }

    @Override
    public Mono<String> getMandateeEmailFromDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        if (credential.has(VC)) {
                            return Mono.just(credential.get(VC).get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATEE).get(EMAIL).asText());
                        } else {
                            return Mono.just(credential.get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATEE).get(EMAIL).asText());                        }
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException());
                    }

                });
    }

    @Override
    public Mono<String> getMandateeFirstNameFromDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        if (credential.has(VC)) {
                            return Mono.just(credential.get(VC).get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATEE).get(FIRST_NAME).asText());
                        } else {
                            return Mono.just(credential.get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATEE).get(FIRST_NAME).asText());
                        }
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException());
                    }

                });
    }

    @Override
    public Mono<String> getMandateeCompleteNameFromDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        if(credential.has(VC)){
                            return Mono.just(credential.get(VC).get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATEE).get(FIRST_NAME).asText() + " " + credential.get(VC).get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATEE).get(LAST_NAME).asText());
                        } else{
                            return Mono.just(credential.get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATEE).get(FIRST_NAME).asText() + " " + credential.get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATEE).get(LAST_NAME).asText());
                        }
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException());
                    }
                });
    }

    //FIXME cambiar correo por el del mandator cuando quede claro que mail usar para no usar el de jesus
    @Override
    public Mono<String> getSignerEmailFromDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        if(credential.has(VC)){
                            if(credential.get(VC).get(CREDENTIAL_SUBJECT).get(MANDATE).has(SIGNER)){
                                return Mono.just(credential.get(VC).get(CREDENTIAL_SUBJECT).get(MANDATE).get(SIGNER).get(EMAIL_ADDRESS).asText());
                            } else {
                                return Mono.just(credential.get(VC).get(ISSUER).get(EMAIL_ADDRESS).asText());
                            }
                        } else {
                            return Mono.just("domesupport@in2.es");
                        }
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
                                .operationMode(credentialProcedure.getOperationMode())
                                .signatureMode(credentialProcedure.getSignatureMode())
                                .credential(objectMapper.readTree(credentialProcedure.getCredentialDecoded()))
                                .build());
                    } catch (JsonProcessingException e) {
                        log.warn(ERROR_PARSING_CREDENTIAL, e);
                        return Mono.error(new JsonParseException(null, ERROR_PARSING_CREDENTIAL));
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
    public Mono<String> getMandatorOrganizationFromDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        if(credential.has(VC)){
                            return Mono.just(credential.get(VC).get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATOR).get(ORGANIZATION).asText());
                        } else {
                            return Mono.just(credential.get(CREDENTIAL_SUBJECT).get(MANDATE).get(MANDATOR).get(ORGANIZATION).asText());
                        }
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException());
                    }
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
                .flatMap(credentialProcedure -> getCredentialTypeByProcedureId(String.valueOf(credentialProcedure.getProcedureId()))
                        .flatMap(credentialType -> Mono.just(ProcedureBasicInfo.builder()
                                .procedureId(credentialProcedure.getProcedureId())
                                .subject(credentialProcedure.getSubject())
                                .credentialType(credentialProcedure.getCredentialType())
                                .status(String.valueOf(credentialProcedure.getCredentialStatus()))
                                .updated(credentialProcedure.getUpdatedAt())
                                .build())))
                .map(procedureBasicInfo -> CredentialProcedures.CredentialProcedure.builder()
                        .credentialProcedure(procedureBasicInfo)
                        .build())
                .collectList()
                .map(CredentialProcedures::new);
    }
}
