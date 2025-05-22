package es.in2.issuer.backend.shared.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.backend.shared.domain.exception.ParseErrorException;
import es.in2.issuer.backend.shared.domain.model.dto.PreSubmittedDataCredentialRequest;
import es.in2.issuer.backend.shared.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.backend.shared.domain.model.entities.CredentialIssuanceRecord;
import es.in2.issuer.backend.shared.domain.repository.CredentialIssuanceRepository;
import es.in2.issuer.backend.shared.domain.service.AccessTokenService;
import es.in2.issuer.backend.shared.domain.service.CredentialIssuanceRecordService;
import es.in2.issuer.backend.shared.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static es.in2.issuer.backend.shared.domain.util.Utils.generateCustomNonce;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialIssuanceRecordServiceImpl implements CredentialIssuanceRecordService {

    private final ObjectMapper objectMapper;
    private final CacheStore<String> cacheStoreForTransactionCode;
    private final CredentialIssuanceRepository credentialIssuanceRepository;
    private final AccessTokenService accessTokenService;

    @Override
    public Mono<String> create(
            String processId,
            PreSubmittedDataCredentialRequest preSubmittedDataCredentialRequest,
            String token) {
        return buildCredentialIssuanceRecord(preSubmittedDataCredentialRequest, token)
                .flatMap(credentialIssuanceRecord ->
                        credentialIssuanceRepository.save(credentialIssuanceRecord)
                                .thenReturn(credentialIssuanceRecord.getId().toString()))
                .flatMap(this::generateActivationCode);
    }

    private Mono<String> generateActivationCode(String credentialIssuanceRecordId) {
        return generateCustomNonce()
                .flatMap(activationCode ->
                        cacheStoreForTransactionCode.add(activationCode, credentialIssuanceRecordId));
    }

    private Mono<CredentialIssuanceRecord> buildCredentialIssuanceRecord(
            PreSubmittedDataCredentialRequest preSubmittedDataCredentialRequest,
            String token) {
        return accessTokenService.getOrganizationId(token)
                .flatMap(organizationIdentifier ->
                        getEmailFromLearCredentialEmployee(preSubmittedDataCredentialRequest.payload())
                                .map(email -> {
                                    Instant now = Instant.now();
                                    CredentialIssuanceRecord credentialIssuanceRecord = new CredentialIssuanceRecord();
                                    credentialIssuanceRecord.setId(UUID.randomUUID());
                                    credentialIssuanceRecord.setOrganizationIdentifier(organizationIdentifier);
                                    credentialIssuanceRecord.setEmail(email);
                                    credentialIssuanceRecord.setCredentialFormat(
                                            preSubmittedDataCredentialRequest.format());
                                    credentialIssuanceRecord.setCredentialType(
                                            preSubmittedDataCredentialRequest.schema());
                                    credentialIssuanceRecord.setCredentialData(
                                            preSubmittedDataCredentialRequest.payload().toString());
                                    credentialIssuanceRecord.setOperationMode(
                                            preSubmittedDataCredentialRequest.operationMode());
                                    // TODO: get signature mode from DDBB
                                    credentialIssuanceRecord.setSignatureMode("TODO");
                                    credentialIssuanceRecord.setCreatedAt(Timestamp.from(now));
                                    credentialIssuanceRecord.setUpdatedAt(Timestamp.from(now));
                                    return credentialIssuanceRecord;
                                }));
    }

    private Mono<String> getEmailFromLearCredentialEmployee(JsonNode payload) {
        try {
            return Mono.just(objectMapper.treeToValue(payload, LEARCredentialEmployee.class)
                    .credentialSubject()
                    .mandate()
                    .mandatee()
                    .email());
        } catch (JsonProcessingException e) {
            return Mono.error(new ParseErrorException("Error parsing preSubmittedDataCredentialRequest payload: " + e));
        }
    }
}
