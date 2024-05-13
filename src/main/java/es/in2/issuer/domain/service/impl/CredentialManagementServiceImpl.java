package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.entity.CredentialDeferred;
import es.in2.issuer.domain.entity.CredentialManagement;
import es.in2.issuer.domain.exception.NoCredentialFoundException;
import es.in2.issuer.domain.exception.ParseCredentialJsonException;
import es.in2.issuer.domain.model.CredentialItem;
import es.in2.issuer.domain.repository.CredentialDeferredRepository;
import es.in2.issuer.domain.repository.CredentialManagementRepository;
import es.in2.issuer.domain.service.CredentialManagementService;
import es.in2.issuer.domain.util.CredentialStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialManagementServiceImpl implements CredentialManagementService {
    private final CredentialManagementRepository credentialManagementRepository;
    private final CredentialDeferredRepository credentialDeferredRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<String> commitCredential(String credential, String userId, String format) {

        String transactionId = UUID.randomUUID().toString();
        CredentialManagement newCredential = CredentialManagement.builder()
                .userId(userId)
                .credentialDecoded(credential)
                .credentialStatus(CredentialStatus.ISSUED.getName())
                .credentialFormat(format)
                .modifiedAt(new Timestamp(Instant.now().toEpochMilli()))
                .build();

        return credentialManagementRepository.save(newCredential)
                .flatMap(savedCredential -> credentialDeferredRepository.save(CredentialDeferred.builder()
                        .transactionId(transactionId)
                        .credentialId(savedCredential.getId())
                        .build()))
                .then(Mono.just(transactionId));
    }

    @Override
    public Mono<Void> updateCredential(String credential, UUID credentialId, String userId) {
        return credentialManagementRepository.findByIdAndUserId(credentialId, userId)
                .switchIfEmpty(Mono.error(new NoCredentialFoundException("No credential found with credentialId: " + credentialId + " and userId: " + userId)))
                .flatMap(existingCredential -> {
                    existingCredential.setCredentialEncoded(credential); // Update credential
                    existingCredential.setCredentialStatus(CredentialStatus.VALID.getName()); // Set status to valid
                    existingCredential.setModifiedAt(new Timestamp(Instant.now().toEpochMilli())); // Update modified time
                    return credentialManagementRepository.save(existingCredential); // Save the updated credential
                })
                .flatMap(savedCredential -> credentialDeferredRepository.findByCredentialId(savedCredential.getId())
                        .flatMap(credentialDeferred -> {
                            credentialDeferred.setCredentialSigned(credential);
                            return credentialDeferredRepository.save(credentialDeferred);
                        }))
                .then(); // Return only completion signal
    }

    @Override
    public Mono<String> updateTransactionId(String transactionId) {
        String newTransactionId = UUID.randomUUID().toString(); // Generate a new transactionId

        return credentialDeferredRepository.findByTransactionId(transactionId)
                .flatMap(deferredCredential -> {
                    deferredCredential.setTransactionId(newTransactionId); // Update transactionId
                    return credentialDeferredRepository.save(deferredCredential); // Save the updated credential
                })
                .map(CredentialDeferred::getTransactionId); // Return new transactionId
    }

    @Override
    public Mono<Void> deleteCredentialDeferred(String transactionId){
        return credentialDeferredRepository.findByTransactionId(transactionId)
                .switchIfEmpty(Mono.error(new NoCredentialFoundException("No credential found with transactionId: " + transactionId)))
                .flatMap(credentialDeferredRepository::delete)
                .then();
    }

    @Override
    public Flux<CredentialItem> getCredentials(String userId, int page, int size, String sort, Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        return credentialManagementRepository.findByUserIdOrderByModifiedAtDesc(userId, pageable)
                .switchIfEmpty(Mono.error(new NoCredentialFoundException("No credential found for userId: " + userId + " at page: " + pageable.getPageNumber())))
                .flatMap(credentialManagement -> parseCredentialJson(credentialManagement.getCredentialDecoded())
                .map(parsedCredential -> CredentialItem.builder()
                        .credentialId(credentialManagement.getId())
                        .credential(parsedCredential)
                        .format(credentialManagement.getCredentialFormat())
                        .status(credentialManagement.getCredentialStatus())
                        .modifiedAt(credentialManagement.getModifiedAt())
                        .build())
                )
                .doOnError(error -> log.error("Could not load credentials, error: {}", error.getMessage()));
    }

    @Override
    public Mono<CredentialItem> getCredential(UUID credentialId, String userId) {
        log.info("Entering getCredential method with credentialId: {} and userId: {}", credentialId, userId);
        return credentialManagementRepository.findByIdAndUserId(credentialId, userId)
                .switchIfEmpty(Mono.error(new NoCredentialFoundException("No credential found with credentialId: " + credentialId + " and userId: " + userId)))
                .flatMap(credentialManagement -> parseCredentialJson(credentialManagement.getCredentialDecoded())
                        .map(parsedCredential -> CredentialItem.builder()
                                .credentialId(credentialManagement.getId())
                                .credential(parsedCredential)
                                .format(credentialManagement.getCredentialFormat())
                                .status(credentialManagement.getCredentialStatus())
                                .modifiedAt(credentialManagement.getModifiedAt())
                                .build())
                )
                .doOnError(error -> log.error("Error in getCredential method: {}", error.getMessage()));
    }

    @Override
    public Mono<CredentialDeferred> getDeferredCredentialByTransactionId(String transactionId) {
        return credentialDeferredRepository.findByTransactionId(transactionId);
    }

    private Mono<Map<String, Object>> parseCredentialJson(String jsonCredential) {
        return Mono.fromCallable(() -> {
                    try {
                        return objectMapper.readValue(jsonCredential, new TypeReference<Map<String, Object>>() {});
                    } catch (JsonProcessingException e) {
                        throw new ParseCredentialJsonException("JSON parsing error");
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())  // This ensures that the blocking operation doesn't block the main thread
                .onErrorMap(e -> new ParseCredentialJsonException("Error parsing JSON: " + e.getMessage()));
    }
}
