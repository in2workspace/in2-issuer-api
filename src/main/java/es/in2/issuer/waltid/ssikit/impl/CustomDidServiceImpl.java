package es.in2.issuer.waltid.ssikit.impl;

import es.in2.issuer.api.utils.Utils;
import es.in2.issuer.waltid.ssikit.CustomDidService;
import es.in2.issuer.waltid.ssikit.CustomKeyService;
import id.walt.model.DidMethod;
import id.walt.servicematrix.ServiceMatrix;
import id.walt.services.did.DidService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomDidServiceImpl implements CustomDidService {

    private final CustomKeyService customKeyService;

    @Override
    public Mono<String> generateDidKey() {
        log.info("DID Service - Generate DID Key");
        return Mono.defer(() -> {
            new ServiceMatrix(Utils.SERVICE_MATRIX_PATH);
            return customKeyService.generateKey()
                    .flatMap(keyId -> Mono.fromSupplier(() -> DidService.INSTANCE.create(DidMethod.key, keyId.getId(), null)))
                    .doOnSuccess(result -> log.info("Success: {}", result))
                    .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()));
        });
    }

    @Override
    public Mono<String> generateDidKeyWithKid(String kid) {
        log.info("DID Service - Generate DID Key by KID");
        new ServiceMatrix(Utils.SERVICE_MATRIX_PATH);
        return Mono.just(DidService.INSTANCE.create(DidMethod.key, kid,null))
                .doOnSuccess(result -> log.info("Success: {}", result))
                .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()));
    }
}
