package es.in2.issuer.waltid.ssikit;

import com.nimbusds.jose.jwk.ECKey;
import id.walt.crypto.KeyId;
import reactor.core.publisher.Mono;

import java.text.ParseException;

public interface CustomKeyService {

    Mono<KeyId> generateKey();

    Mono<ECKey> getECKeyFromKid(String kid) throws ParseException;
}
