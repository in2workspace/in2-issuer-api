package es.in2.issuer.domain.service;

import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;

public interface JWTService {

    String generateJWT(String payload);

    SignedJWT parseJWT(String jwt);

    Payload getPayloadFromSignedJWT(SignedJWT signedJWT);

    String getClaimFromPayload(Payload payload, String claimName);

}
