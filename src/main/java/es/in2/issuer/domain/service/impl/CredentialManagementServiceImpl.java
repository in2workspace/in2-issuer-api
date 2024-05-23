//package es.in2.issuer.domain.service.impl;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import es.in2.issuer.domain.entity.CredentialProcedure;
//import es.in2.issuer.domain.entity.DeferredCredentialMetadata;
//import es.in2.issuer.domain.exception.NoCredentialFoundException;
//import es.in2.issuer.domain.exception.ParseCredentialJsonException;
//import es.in2.issuer.domain.model.CredentialItem;
//import es.in2.issuer.infrastructure.repository.DeferredCredentialMetadataRepository;
//import es.in2.issuer.infrastructure.repository.CredentialProcedureRepository;
//import es.in2.issuer.domain.service.CredentialManagementService;
//import es.in2.issuer.domain.util.CredentialStatus;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import reactor.core.scheduler.Schedulers;
//
//import java.sql.Timestamp;
//import java.time.Instant;
//import java.util.Base64;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class CredentialManagementServiceImpl implements CredentialManagementService {
//    private final CredentialProcedureRepository credentialProcedureRepository;
//    private final DeferredCredentialMetadataRepository deferredCredentialMetadataRepository;
//    private final ObjectMapper objectMapper;
//
//    @Override
//    public Mono<String> commitCredential(String credential, String userId, String format) {
//
//        String transactionId = UUID.randomUUID().toString();
//        DeferredCredentialMetadata newCredential = DeferredCredentialMetadata.builder()
//                .credentialDecoded(credential)
//                .credentialStatus(CredentialStatus.ISSUED.getName())
//                .credentialFormat(format)
//                .modifiedAt(new Timestamp(Instant.now().toEpochMilli()))
//                .build();
//
//        return credentialProcedureRepository.save(newCredential)
//                .flatMap(savedCredential -> deferredCredentialMetadataRepository.save(CredentialProcedure.builder()
//                        .transactionId(transactionId)
//                        .credentialId(savedCredential.getId())
//                        .build()))
//                .then(Mono.just(transactionId));
//    }
//
//    @Override
//    public Mono<Void> updateCredential(String credential, UUID credentialId, String userId) {
//        return credentialProcedureRepository.findByIdAndUserId(credentialId, userId)
//                .switchIfEmpty(Mono.error(new NoCredentialFoundException("No credential found with credentialId: " + credentialId + " and userId: " + userId)))
//                .flatMap(existingCredential -> {
//                    existingCredential.setCredentialEncoded(credential); // Update credential
//                    existingCredential.setCredentialStatus(CredentialStatus.VALID.getName()); // Set status to valid
//                    existingCredential.setModifiedAt(new Timestamp(Instant.now().toEpochMilli())); // Update modified time
//                    return credentialProcedureRepository.save(existingCredential); // Save the updated credential
//                })
//                .flatMap(savedCredential -> deferredCredentialMetadataRepository.findByCredentialId(savedCredential.getId())
//                        .flatMap(credentialDeferred -> {
//                            credentialDeferred.setCredentialSigned(credential);
//                            return deferredCredentialMetadataRepository.save(credentialDeferred);
//                        }))
//                .then(); // Return only completion signal
//    }
//
//    @Override
//    public Mono<Void> updateCredentials(SignedCredentials signedCredentials, String userId) {
//        List<SignedCredentials.SignedCredential> credentials = signedCredentials.credentials();
//
//        return Flux.fromIterable(credentials)
//                .flatMap(signedCredential -> {
//                    String jwtToken = signedCredential.credential();
//                    return extractJtiFromToken(jwtToken)
//                            .flatMap(jti -> credentialManagementRepository
//                                    .findByUserIdAndCredentialDecodedContains(userId, jti)
//                                    .flatMap(credentialManagement -> {
//                                        credentialManagement.setCredentialEncoded(jwtToken);
//                                        credentialManagement.setCredentialStatus(CredentialStatus.VALID.getName());
//                                        credentialManagement.setModifiedAt(new Timestamp(Instant.now().toEpochMilli()));
//                                        return credentialManagementRepository.save(credentialManagement);
//                                    })
//                                    .flatMap(savedCredential -> credentialDeferredRepository.findByCredentialId(savedCredential.getId())
//                                            .flatMap(credentialDeferred -> {
//                                                credentialDeferred.setCredentialSigned(jwtToken);
//                                                return credentialDeferredRepository.save(credentialDeferred);
//                                            }))
//                            );
//                })
//                .then();
//    }
//
//    @Override
//    public Mono<String> updateTransactionId(String transactionId) {
//        String newTransactionId = UUID.randomUUID().toString(); // Generate a new transactionId
//
//        return deferredCredentialMetadataRepository.findByTransactionId(transactionId)
//                .flatMap(deferredCredential -> {
//                    deferredCredential.setTransactionId(newTransactionId); // Update transactionId
//                    return deferredCredentialMetadataRepository.save(deferredCredential); // Save the updated credential
//                })
//                .map(CredentialProcedure::getTransactionId); // Return new transactionId
//    }
//
//    @Override
//    public Mono<Void> deleteCredentialDeferred(String transactionId){
//        return deferredCredentialMetadataRepository.findByTransactionId(transactionId)
//                .switchIfEmpty(Mono.error(new NoCredentialFoundException("No credential found with transactionId: " + transactionId)))
//                .flatMap(deferredCredentialMetadataRepository::delete)
//                .then();
//    }
//
//    @Override
//    public Flux<CredentialItem> getCredentials(String userId, int page, int size, String sort, Sort.Direction direction) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
//        return credentialProcedureRepository.findByUserIdOrderByModifiedAtDesc(userId, pageable)
//                .switchIfEmpty(Mono.error(new NoCredentialFoundException("No credential found for userId: " + userId + " at page: " + pageable.getPageNumber())))
//                .flatMap(deferredCredentialMetadata -> parseCredentialJson(deferredCredentialMetadata.getCredentialDecoded())
//                .map(parsedCredential -> CredentialItem.builder()
//                        .credentialId(deferredCredentialMetadata.getId())
//                        .credential(parsedCredential)
//                        .format(deferredCredentialMetadata.getCredentialFormat())
//                        .status(deferredCredentialMetadata.getCredentialStatus())
//                        .modifiedAt(deferredCredentialMetadata.getModifiedAt())
//                        .build())
//                )
//                .doOnError(error -> log.error("Could not load credentials, error: {}", error.getMessage()));
//    }
//
//    @Override
//    public Mono<PendingCredentials> getPendingCredentials(String userId, int page, int size, String sort, Sort.Direction direction) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));
//        return credentialManagementRepository.findByUserIdAndCredentialStatusOrderByModifiedAtDesc(userId, CredentialStatus.ISSUED.getName(), pageable)
//                .switchIfEmpty(Mono.error(new NoCredentialFoundException("No credential found for userId: " + userId + " at page: " + pageable.getPageNumber())))
//                .map(CredentialManagement::getCredentialDecoded)
//                .flatMap(credentialString -> verifiableCredentialService.generateDeferredVcPayLoad(credentialString)
//                        .flatMap(this::parseCredentialJson))
//                .collectList()  // Collect the Map<String, Object> results into a list
//                .map(credentials -> new PendingCredentials(
//                        credentials.stream()
//                                .map(PendingCredentials.CredentialPayload::new)
//                                .toList()
//                ))
//                .doOnError(error -> log.error("Could not load credentials, error: {}", error.getMessage()));
//    }
//    @Override
//    public Mono<CredentialItem> getCredential(UUID credentialId, String userId) {
//        log.info("Entering getCredential method with credentialId: {} and userId: {}", credentialId, userId);
//        return credentialProcedureRepository.findByIdAndUserId(credentialId, userId)
//                .switchIfEmpty(Mono.error(new NoCredentialFoundException("No credential found with credentialId: " + credentialId + " and userId: " + userId)))
//                .flatMap(deferredCredentialMetadata -> parseCredentialJson(deferredCredentialMetadata.getCredentialDecoded())
//                        .map(parsedCredential -> CredentialItem.builder()
//                                .credentialId(deferredCredentialMetadata.getId())
//                                .credential(parsedCredential)
//                                .format(deferredCredentialMetadata.getCredentialFormat())
//                                .status(deferredCredentialMetadata.getCredentialStatus())
//                                .modifiedAt(deferredCredentialMetadata.getModifiedAt())
//                                .build())
//                )
//                .doOnError(error -> log.error("Error in getCredential method: {}", error.getMessage()));
//    }
//
//    @Override
//    public Mono<CredentialProcedure> getDeferredCredentialByTransactionId(String transactionId) {
//        return deferredCredentialMetadataRepository.findByTransactionId(transactionId);
//    }
//
//    private Mono<Map<String, Object>> parseCredentialJson(String jsonCredential) {
//        return Mono.fromCallable(() -> {
//                    try {
//                        return objectMapper.readValue(jsonCredential, new TypeReference<Map<String, Object>>() {});
//                    } catch (JsonProcessingException e) {
//                        throw new ParseCredentialJsonException("JSON parsing error");
//                    }
//                })
//                .subscribeOn(Schedulers.boundedElastic())  // This ensures that the blocking operation doesn't block the main thread
//                .onErrorMap(e -> new ParseCredentialJsonException("Error parsing JSON: " + e.getMessage()));
//    }
//
//    private Mono<String> extractJtiFromToken(String jwtToken) {
//        try {
//            String[] parts = jwtToken.split("\\.");
//            if (parts.length != 3) {
//                return Mono.error(new IllegalArgumentException("Invalid JWT token"));
//            }
//            String payload = new String(Base64.getDecoder().decode(parts[1]));
//            JsonNode jsonNode = objectMapper.readTree(payload);
//            String jti = jsonNode.get("jti").asText();
//            return Mono.just(jti);
//        } catch (Exception e) {
//            return Mono.error(e);
//        }
//    }
//}
