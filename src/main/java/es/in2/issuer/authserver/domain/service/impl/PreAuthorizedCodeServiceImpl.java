package es.in2.issuer.authserver.domain.service.impl;

import es.in2.issuer.authserver.domain.service.PreAuthorizedCodeCacheStore;
import es.in2.issuer.authserver.domain.service.PreAuthorizedCodeService;
import es.in2.issuer.shared.domain.model.dto.Grant;
import es.in2.issuer.shared.domain.model.dto.PreAuthorizedCodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.UUID;

import static es.in2.issuer.authserver.domain.util.Constants.*;
import static es.in2.issuer.shared.domain.util.Utils.generateCustomNonce;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreAuthorizedCodeServiceImpl implements PreAuthorizedCodeService {
    private final SecureRandom random;
    private final PreAuthorizedCodeCacheStore preAuthorizedCodeCacheStore;

    @Override
    public Mono<PreAuthorizedCodeResponse> generatePreAuthorizedCodeResponse(String processId, UUID credentialId) {
        return generatePreAuthorizedCode()
                .zipWith(generateTxCode())
                .flatMap(tuple -> {
                    String preAuthorizedCode = tuple.getT1();
                    String txCode = tuple.getT2();
                    log.debug("ProcessId: {} AuthServer: PreAuthorizedCode and TxCode generated", processId);

                    return preAuthorizedCodeCacheStore.save(processId, preAuthorizedCode, credentialId, txCode)
                            .flatMap(preAuthorizedCodeSaved ->
                                    buildPreAuthorizedCodeResponse(preAuthorizedCodeSaved, txCode));
                });
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
