package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.entity.Credential;
import es.in2.issuer.domain.repository.CredentialRepository;
import es.in2.issuer.domain.service.CredentialManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.NoSuchElementException;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialManagementServiceImpl implements CredentialManagementService {
    private final CredentialRepository credentialRepository;
    @Override
    public Mono<String> commitCredential(String credential, String userId) {

        String transactionId = UUID.randomUUID().toString();
        Credential newCredential = new Credential();
        newCredential.setUserId(userId);
        newCredential.setCredentialData(credential);
        newCredential.setStatus(CREDENTIAL_DOWNLOADED);
        newCredential.setTransactionId(transactionId);
        newCredential.setCreatedAt(new Date());  // Setting current date as creation date
        newCredential.setModifiedAt(new Date());  // Setting current date as modified date

        return credentialRepository.save(newCredential)
                .map(savedCredential -> savedCredential.getTransactionId());  // Extracting and returning transactionId
    }

    @Override
    public Mono<Void> updateCredential(String credential, Long credentialId, String userId) {
        return credentialRepository.findById(credentialId)
                .switchIfEmpty(Mono.error(new NoSuchElementException("No credential found with credentialId: " + credentialId + " and userId: " + userId)))
                .flatMap(existingCredential -> {
                    existingCredential.setCredentialData(credential); // Update credential transactionId
                    existingCredential.setStatus(CREDENTIAL_SIGNED); // Set status to signed
                    return credentialRepository.save(existingCredential); // Save the updated credential
                })
                .then(); // Return only completion signal
    }

    @Override
    public Mono<String> updateTransactionId(String transactionId, String userId) {
        String newTransactionId = UUID.randomUUID().toString(); // Generate a new transactionId

        return credentialRepository.findByTransactionIdAndUserId(transactionId, userId)
                .flatMap(existingCredential -> {
                    existingCredential.setTransactionId(newTransactionId); // Update transactionId
                    return credentialRepository.save(existingCredential); // Save the updated credential
                })
                .map(updatedCredential -> updatedCredential.getTransactionId()); // Return new transactionId
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
    public Flux<Credential> getCredentials(String userId) {
        return credentialRepository.findByUserIdOrderByModifiedAtDesc(userId);
    }

    @Override
    public Mono<Credential> getCredential(Long credentialId) {
        return credentialRepository.findById(credentialId);
    }

    @Override
    public Mono<Credential> getCredentialByTransactionId(String transactionId, String userId) {
        return credentialRepository.findByTransactionIdAndUserId(transactionId, userId);
    }
}
