package es.in2.issuer.oidc4vci.domain.service.impl;

import es.in2.issuer.oidc4vci.domain.service.PreAuthorizedCodeService;
import es.in2.issuer.shared.domain.model.dto.CredentialIdAndTxCode;
import es.in2.issuer.shared.domain.model.dto.Grant;
import es.in2.issuer.shared.domain.model.dto.PreAuthorizedCodeResponse;
import es.in2.issuer.shared.infrastructure.repository.CacheStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.security.SecureRandom;
import java.util.UUID;

import static es.in2.issuer.oidc4vci.domain.util.Constants.*;
import static es.in2.issuer.shared.domain.util.Utils.generateCustomNonce;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreAuthorizedCodeServiceImpl implements PreAuthorizedCodeService {
    private final SecureRandom random;
    private final CacheStoreRepository<CredentialIdAndTxCode> credentialIdAndTxCodeByPreAuthorizedCodeCacheStore;

    @Override
    public Mono<PreAuthorizedCodeResponse> generatePreAuthorizedCode(
            String processId,
            Mono<UUID> credentialIdMono) {
        return generateCodes()
                .doFirst(() ->
                        log.debug("ProcessId: {} AuthServer: Generating PreAuthorizedCode response", processId))
                .flatMap(tuple -> credentialIdMono
                        .flatMap(credentialId -> {
                            String preAuthorizedCode = tuple.getT1();
                            String txCode = tuple.getT2();
                            return storeInCache(processId, credentialId, preAuthorizedCode, txCode)
                                    .flatMap(preAuthorizedCodeSaved ->
                                            buildPreAuthorizedCodeResponse(preAuthorizedCodeSaved, txCode));
                        }))
                .doOnSuccess(preAuthorizedCodeResponse ->
                        log.debug(
                                "ProcessId: {} AuthServer: Generated PreAuthorizedCode response successfully",
                                processId));
    }

    private @NotNull Mono<Tuple2<String, String>> generateCodes() {
        return Mono.zip(generatePreAuthorizedCode(), generateTxCode());
    }

    private @NotNull Mono<String> storeInCache(String processId, UUID credentialId, String preAuthorizedCode, String txCode) {
        return credentialIdAndTxCodeByPreAuthorizedCodeCacheStore
                .add(preAuthorizedCode, new CredentialIdAndTxCode(credentialId, txCode))
                .doOnSuccess(preAuthorizedCodeSaved ->
                        log.debug(
                                "ProcessId: {} AuthServer: Saved TxCode and CredentialId by " +
                                        "PreAuthorizedCode in cache",
                                processId));
    }

    private Mono<String> generatePreAuthorizedCode() {
        return generateCustomNonce();
    }

    private Mono<String> generateTxCode() {
        double minValue = Math.pow(10, (double) TX_CODE_SIZE - 1);
        double maxValue = Math.pow(10, TX_CODE_SIZE) - 1;
        int i = random.nextInt((int) (maxValue - minValue + 1)) + (int) minValue;
        return Mono.just(String.valueOf(i));
    }

    private Mono<PreAuthorizedCodeResponse> buildPreAuthorizedCodeResponse(String preAuthorizedCode, String txCode) {
        Grant.TxCode grantTxCode = new Grant.TxCode(TX_CODE_SIZE, TX_INPUT_MODE, TX_CODE_DESCRIPTION);
        Grant grant = new Grant(preAuthorizedCode, grantTxCode);
        return Mono.just(new PreAuthorizedCodeResponse(grant, txCode));
    }
}
