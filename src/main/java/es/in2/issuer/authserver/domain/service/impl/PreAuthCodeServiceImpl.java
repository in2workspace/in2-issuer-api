package es.in2.issuer.authserver.domain.service.impl;

import es.in2.issuer.authserver.domain.service.PreAuthCodeCacheStore;
import es.in2.issuer.authserver.domain.service.PreAuthCodeService;
import es.in2.issuer.shared.domain.model.dto.Grant;
import es.in2.issuer.shared.domain.model.dto.PreAuthCodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;

import static es.in2.issuer.authserver.domain.utils.Constants.*;
import static es.in2.issuer.shared.domain.util.Utils.generateCustomNonce;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreAuthCodeServiceImpl implements PreAuthCodeService {
    private final SecureRandom random;
    private final PreAuthCodeCacheStore preAuthCodeCacheStore;

    @Override
    public Mono<PreAuthCodeResponse> generatePreAuthCodeResponse(String processId) {
        return generatePreAuthCode()
                .zipWith(generatePinTxCode())
                .flatMap(tuple -> {
                    String preAuthCode = tuple.getT1();
                    String pinTxCode = tuple.getT2();
                    log.debug("ProcessId: {} AuthServer: Pre Auth Code and pin (TX Code) generated", processId);

                    return preAuthCodeCacheStore.save(processId, preAuthCode, pinTxCode)
                            .flatMap(preAuthCodeSaved ->
                                    buildPreAuthCodeResponse(preAuthCodeSaved, pinTxCode));
                });
    }

    private Mono<String> generatePreAuthCode() {
        return generateCustomNonce();
    }

    private Mono<String> generatePinTxCode() {
        double minValue = Math.pow(10, (double) TX_CODE_SIZE - 1);
        double maxValue = Math.pow(10, TX_CODE_SIZE) - 1;
        int i = random.nextInt((int) (maxValue - minValue + 1)) + (int) minValue;
        return Mono.just(String.valueOf(i));
    }

    private Mono<PreAuthCodeResponse> buildPreAuthCodeResponse(String preAuthCode, String pinTxCode) {
        Grant.TxCode txCode = new Grant.TxCode(TX_CODE_SIZE, TX_INPUT_MODE, TX_CODE_DESCRIPTION);
        Grant grant = new Grant(preAuthCode, txCode);
        return Mono.just(new PreAuthCodeResponse(grant, pinTxCode));
    }
}
