package es.in2.issuer.shared.domain.service;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import reactor.core.publisher.Mono;

public interface JWTService {

    String generateJWT(String payload);
    Mono<Boolean> validateJwtSignatureReactive(JWSObject jwsObject);

    SignedJWT parseJWT(String jwt);

    Payload getPayloadFromSignedJWT(SignedJWT signedJWT);

    String getClaimFromPayload(Payload payload, String claimName);

    Long getExpirationFromToken(String token);
}
