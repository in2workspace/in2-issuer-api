package es.in2.issuer.backend.oidc4vci.domain.service.impl;

import es.in2.issuer.backend.oidc4vci.domain.service.PreAuthorizedCodeService;
import es.in2.issuer.backend.shared.domain.model.dto.CredentialIdAndTxCode;
import es.in2.issuer.backend.shared.domain.model.dto.Grants;
import es.in2.issuer.backend.shared.domain.model.dto.PreAuthorizedCodeResponse;
import es.in2.issuer.backend.shared.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.security.SecureRandom;
import java.util.UUID;

import static es.in2.issuer.backend.oidc4vci.domain.util.Constants.*;
import static es.in2.issuer.backend.shared.domain.util.Utils.generateCustomNonce;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreAuthorizedCodeServiceImpl implements PreAuthorizedCodeService {
    private final SecureRandom random;
    private final CacheStore<CredentialIdAndTxCode> credentialIdAndTxCodeByPreAuthorizedCodeCacheStore;

    // TODO: retornar preauthorizedcode
    @Override
    public Mono<PreAuthorizedCodeResponse> generatePreAuthorizedCode(String processId, Mono<UUID> credentialIdMono) {
        return generateCodes()
                .doFirst(() -> log.debug("ProcessId: {} AuthServer: Generating PreAuthorizedCode response", processId))
                .flatMap(tuple -> storeInCache(processId, credentialIdMono, tuple))
                .doOnSuccess(preAuthorizedCodeResponse ->
                        log.debug(
                                "ProcessId: {} AuthServer: Generated PreAuthorizedCode response successfully",
                                processId));
    }

    private @NotNull Mono<Tuple2<String, String>> generateCodes() {
        return Mono.zip(generatePreAuthorizedCode(), generateTxCode());
    }

    private @NotNull Mono<PreAuthorizedCodeResponse> storeInCache(String processId, Mono<UUID> credentialIdMono, Tuple2<String, String> codes) {
        String preAuthorizedCode = codes.getT1();
        String txCode = codes.getT2();

        return credentialIdMono
                .flatMap(credentialId ->
                        credentialIdAndTxCodeByPreAuthorizedCodeCacheStore
                                .add(preAuthorizedCode, new CredentialIdAndTxCode(credentialId, txCode))
                                .doOnSuccess(preAuthorizedCodeSaved ->
                                        log.debug(
                                                "ProcessId: {} AuthServer: Saved TxCode and CredentialId by " +
                                                        "PreAuthorizedCode in cache",
                                                processId))
                                .flatMap(preAuthorizedCodeSaved -> buildPreAuthorizedCodeResponse(
                                        preAuthorizedCodeSaved,
                                        txCode)));
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

    // TODO: NOMÉS retorna preAuthorizedCode i pin
    private Mono<PreAuthorizedCodeResponse> buildPreAuthorizedCodeResponse(String preAuthorizedCode, String txCode) {
        Grants.TxCode grantTxCode = new Grants.TxCode(TX_CODE_SIZE, TX_INPUT_MODE, TX_CODE_DESCRIPTION);
        Grants grants = new Grants(preAuthorizedCode, grantTxCode);
        return Mono.just(new PreAuthorizedCodeResponse(grants, txCode));
    }
}
