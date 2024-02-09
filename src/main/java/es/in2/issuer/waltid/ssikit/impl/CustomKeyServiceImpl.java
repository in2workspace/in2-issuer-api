package es.in2.issuer.waltid.ssikit.impl;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import es.in2.issuer.api.utils.Utils;
import es.in2.issuer.waltid.ssikit.CustomKeyService;
import id.walt.crypto.KeyAlgorithm;
import id.walt.crypto.KeyId;
import id.walt.servicematrix.ServiceMatrix;
import id.walt.services.key.KeyFormat;
import id.walt.services.key.KeyService;
import id.walt.services.keystore.KeyType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.ParseException;
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomKeyServiceImpl implements CustomKeyService {
    @Override
    public Mono<KeyId> generateKey() {
        new ServiceMatrix(Utils.SERVICE_MATRIX_PATH);
        return Mono.just(KeyService.Companion.getService().generate(KeyAlgorithm.ECDSA_Secp256r1))
                .doOnSuccess(result -> log.info("Success: {}", result))
                .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()));
    }

    @Override
    public Mono<ECKey> getECKeyFromKid(String kid) throws ParseException {
        new ServiceMatrix(Utils.SERVICE_MATRIX_PATH);
        String jwk = KeyService.Companion.getService().export(kid, KeyFormat.JWK, KeyType.PRIVATE);
        return Mono.just(JWK.parse(jwk).toECKey())
                .doOnSuccess(result -> log.info("Success: {}", result))
                .doOnError(throwable -> log.error("Error: {}", throwable.getMessage()));
    }
}
