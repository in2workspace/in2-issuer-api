package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.entity.Credential;
import es.in2.issuer.domain.entity.CredentialDeferred;
import es.in2.issuer.domain.entity.CredentialListItem;
import es.in2.issuer.domain.entity.CredentialManagement;
import es.in2.issuer.domain.repository.CredentialDeferredRepository;
import es.in2.issuer.domain.repository.CredentialManagementRepository;
import es.in2.issuer.domain.repository.CredentialRepository;
import es.in2.issuer.domain.service.CredentialManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialManagementServiceImpl implements CredentialManagementService {
    private final CredentialRepository credentialRepository;
    private final CredentialManagementRepository credentialManagementRepository;
    private final CredentialDeferredRepository credentialDeferredRepository;
//    @Override
//    public Mono<String> commitCredential(String credential, String userId) {
//
//        String transactionId = UUID.randomUUID().toString();
//        Credential newCredential = new Credential();
//        newCredential.setUserId(userId);
//        newCredential.setCredentialData(credential);
//        newCredential.setStatus(CREDENTIAL_DOWNLOADED);
//        newCredential.setTransactionId(transactionId);
//        newCredential.setCreatedAt(new Timestamp(Instant.now().toEpochMilli()));  // Setting current date as creation date
//        newCredential.setModifiedAt(new Timestamp(Instant.now().toEpochMilli()));  // Setting current date as modified date
//
//        return credentialRepository.save(newCredential)
//                .map(savedCredential -> savedCredential.getTransactionId());  // Extracting and returning transactionId
//    }
    @Override
    public Mono<String> commitCredential(String credential, String userId) {

        String transactionId = UUID.randomUUID().toString();
        CredentialManagement newCredential = new CredentialManagement();
        newCredential.setUserId(userId);
        newCredential.setCredentialDecoded(credential);
        newCredential.setCredentialStatus(CREDENTIAL_ISSUED);
        newCredential.setModifiedAt(new Timestamp(Instant.now().toEpochMilli()));  // Setting current date as modified date

        CredentialDeferred newCredentialDeferred = new CredentialDeferred();
        newCredentialDeferred.setTransactionId(transactionId);

        return credentialManagementRepository.save(newCredential)
                .flatMap(savedCredential -> {
                    newCredentialDeferred.setCredentialId(savedCredential.getId());
                    return credentialDeferredRepository.save(newCredentialDeferred);
                })
                .then(Mono.just(transactionId));
    }

//    @Override
//    public Mono<Void> updateCredential(String credential, UUID credentialId, String userId) {
//        return credentialRepository.findByIdAndUserId(credentialId, userId)
//                .switchIfEmpty(Mono.error(new NoSuchElementException("No credential found with credentialId: " + credentialId + " and userId: " + userId)))
//                .flatMap(existingCredential -> {
//                    existingCredential.setCredentialData(credential); // Update credential transactionId
//                    existingCredential.setStatus(CREDENTIAL_SIGNED); // Set status to signed
//                    existingCredential.setModifiedAt(new Timestamp(Instant.now().toEpochMilli())); // Update modified time
//                    return credentialRepository.save(existingCredential); // Save the updated credential
//                })
//                .then(); // Return only completion signal
//    }
    @Override
    public Mono<Void> updateCredential(String credential, UUID credentialId, String userId) {
        return credentialManagementRepository.findByIdAndUserId(credentialId, userId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("No credential found with credentialId: " + credentialId + " and userId: " + userId)))
                .flatMap(existingCredential -> {
                    existingCredential.setCredentialEncoded(credential); // Update credential
                    existingCredential.setCredentialStatus(CREDENTIAL_VALID); // Set status to valid
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

//    @Override
//    public Mono<String> updateTransactionId(String transactionId, String userId) {
//        String newTransactionId = UUID.randomUUID().toString(); // Generate a new transactionId
//
//        return credentialRepository.findByTransactionIdAndUserId(transactionId, userId)
//                .flatMap(existingCredential -> {
//                    existingCredential.setTransactionId(newTransactionId); // Update transactionId
//                    return credentialRepository.save(existingCredential); // Save the updated credential
//                })
//                .map(updatedCredential -> updatedCredential.getTransactionId()); // Return new transactionId
//    }
    @Override
    public Mono<String> updateTransactionId(String transactionId) {
        String newTransactionId = UUID.randomUUID().toString(); // Generate a new transactionId

        return credentialDeferredRepository.findByTransactionId(transactionId)
                .flatMap(deferredCredential -> {
                    deferredCredential.setTransactionId(newTransactionId); // Update transactionId
                    return credentialDeferredRepository.save(deferredCredential); // Save the updated credential
                })
                .map(updateDeferredCredential -> updateDeferredCredential.getTransactionId()); // Return new transactionId
    }

    @Override
    public Mono<Void> setToEmitted(String transactionId, String userId) {
        return credentialRepository.findByTransactionIdAndUserId(transactionId, userId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("No credential found with transactionId: " + transactionId + " and userId: " + userId)))
                .flatMap(existingCredential -> {
                    existingCredential.setStatus(CREDENTIAL_EMITTED); // Set status to emitted
                    existingCredential.setTransactionId(null); // Nullify transactionId
                    return credentialRepository.save(existingCredential); // Save the updated credential
                })
                .then(); // Return only completion signal
    }

    @Override
    public Mono<Void> deleteCredentialDeferred(String transactionId){
        return credentialDeferredRepository.findByTransactionId(transactionId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("No credential found with transactionId: " + transactionId)))
                .flatMap(credentialDeferredRepository::delete)
                .then();
    }

    @Override
    public Flux<CredentialListItem> getCredentials(String userId, int page, int size, String sort, Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        return credentialRepository.findByUserIdOrderByModifiedAtDesc(userId, pageable)
                .doOnError(error -> log.error("Service layer error: {}", error.getMessage()));
    }

    @Override
    public Mono<Credential> getCredential(UUID credentialId, String userId) {
        log.info("Entering getCredential method with credentialId: {} and userId: {}", credentialId, userId);
        return credentialRepository.findByIdAndUserId(credentialId, userId)
                .doOnError(error -> log.error("Error in getCredential method: {}", error.getMessage()));
    }

//    @Override
//    public Mono<Credential> getCredentialByTransactionId(String transactionId, String userId) {
//        return credentialRepository.findByTransactionIdAndUserId(transactionId, userId);
//    }
    @Override
    public Mono<CredentialDeferred> getDeferredCredentialByTransactionId(String transactionId) {
        return credentialDeferredRepository.findByTransactionId(transactionId);
    }
}
