package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.entity.DeferredCredentialMetadata;
import es.in2.issuer.domain.exception.TransactionCodeNotFoundException;
import es.in2.issuer.domain.repository.DeferredCredentialMetadataRepository;
import es.in2.issuer.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static es.in2.issuer.domain.util.Utils.generateCustomNonce;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeferredCredentialMetadataServiceImpl implements DeferredCredentialMetadataService {
    private final DeferredCredentialMetadataRepository deferredCredentialMetadataRepository;
    private final CacheStore<String> cacheStore;

    @Override
    public Mono<Void> validateTransactionCode(String transactionCode){
        return cacheStore.get(transactionCode).flatMap(transaction -> {
            if(transaction != null){
                log.debug("The transaction code {} was consumed", transaction);
                cacheStore.delete(transaction);
                return Mono.empty();
            }
            else {
                return Mono.error(new TransactionCodeNotFoundException("Session not found for transaction code: " + transactionCode));
            }
        }
        );
    }

    @Override
    public Mono<Void> updateAuthServerNonceByAuthServerNonce(String accessToken, String preAuthCode) {
        return deferredCredentialMetadataRepository.findByAuthServerNonce(preAuthCode)
                .flatMap(deferredCredentialMetadata -> {
                    deferredCredentialMetadata.setAuthServerNonce(accessToken);
                    return deferredCredentialMetadataRepository.save(deferredCredentialMetadata)
                            .then();
                });
    }

    @Override
    public Mono<String> createDeferredCredentialMetadata(String procedureId) {
        return generateCustomNonce()
                .flatMap(nonce -> cacheStore.add(nonce, nonce))
                .flatMap(transactionCode -> {
                    DeferredCredentialMetadata deferredCredentialMetadata = DeferredCredentialMetadata
                            .builder()
                            .id(UUID.randomUUID())
                            .procedureId(UUID.fromString(procedureId))
                            .transactionCode(transactionCode)
                            .build();
                    return deferredCredentialMetadataRepository.save(deferredCredentialMetadata)
                            .then(Mono.just(transactionCode));
                });
    }

    @Override
    public Mono<String> getProcedureIdByTransactionCode(String transactionCode) {
        return deferredCredentialMetadataRepository.findByTransactionCode(transactionCode)
                .flatMap(deferredCredentialMetadata -> Mono.just(deferredCredentialMetadata.getProcedureId().toString()));
    }

    @Override
    public Mono<String> getProcedureIdByAuthServerNonce(String authServerNonce) {
        return deferredCredentialMetadataRepository.findByAuthServerNonce(authServerNonce)
                .flatMap(deferredCredentialMetadata -> Mono.just(deferredCredentialMetadata.getProcedureId().toString()));
    }

    @Override
    public Mono<Void> updateAuthServerNonceByTransactionCode(String transactionCode, String authServerNonce) {
        return deferredCredentialMetadataRepository.findByTransactionCode(transactionCode)
                .flatMap(deferredCredentialMetadata -> {
                    deferredCredentialMetadata.setAuthServerNonce(authServerNonce);
                    return deferredCredentialMetadataRepository.save(deferredCredentialMetadata)
                            .then();
                });
    }

    @Override
    public Mono<String> updateDeferredCredentialMetadataByAuthServerNonce(String authServerNonce, String format) {
        String transactionId = UUID.randomUUID().toString();
        return deferredCredentialMetadataRepository.findByAuthServerNonce(authServerNonce)
                .flatMap(deferredCredentialMetadata -> {
                    deferredCredentialMetadata.setTransactionId(transactionId);
                    deferredCredentialMetadata.setVcFormat(format);
                    return deferredCredentialMetadataRepository.save(deferredCredentialMetadata)
                            .then(Mono.just(transactionId));
                });
    }


}
