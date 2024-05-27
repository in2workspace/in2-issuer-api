package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.entities.CredentialProcedure;
import es.in2.issuer.domain.model.enums.CredentialStatus;
import es.in2.issuer.infrastructure.repository.CredentialProcedureRepository;
import es.in2.issuer.domain.service.CredentialProcedureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

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
                        JsonNode typeNode = credential.get("type");
                        if (typeNode != null && typeNode.isArray()) {
                            String credentialType = null;
                            for (JsonNode type : typeNode) {
                                if (!type.asText().equals("VerifiableCredential") && !type.asText().equals("VerifiableAttestation")) {
                                    credentialType = type.asText();
                                    break;
                                }
                            }
                            return Mono.justOrEmpty(credentialType);
                        }
                        else {
                            return Mono.error(new RuntimeException("The credential type is missing"));
                        }
                    } catch (JsonProcessingException e) {
                        return Mono.error(new RuntimeException(e));
                    }

                });
    }

    @Override
    public Mono<Void> updateDecodedCredentialByProcedureId(String procedureId, String credential) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    credentialProcedure.setCredentialDecoded(credential);
                    credentialProcedure.setCredentialStatus(CredentialStatus.ISSUED);
                    credentialProcedure.setUpdatedAt(new Timestamp(Instant.now().toEpochMilli()));
                    return credentialProcedureRepository.save(credentialProcedure)
                            .then();
                });
    }

    @Override
    public Mono<String> getDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure ->Mono.just(credentialProcedure.getCredentialDecoded()));
    }

    @Override
    public Mono<String> getMandateeEmailFromDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        return Mono.just(credential.get("credentialSubject").get("mandate").get("mandatee").get("email").toString());
                    } catch (JsonProcessingException e){
                        return Mono.error(new RuntimeException());
                    }

                });
    }

    @Override
    public Mono<String> getMandatorEmailFromDecodedCredentialByProcedureId(String procedureId) {
        return credentialProcedureRepository.findById(UUID.fromString(procedureId))
                .flatMap(credentialProcedure -> {
                    try {
                        JsonNode credential = objectMapper.readTree(credentialProcedure.getCredentialDecoded());
                        return Mono.just(credential.get("credentialSubject").get("mandate").get("mandator").get("emailAddress").toString());
                    } catch (JsonProcessingException e){
                        return Mono.error(new RuntimeException());
                    }

                });
    }

}
