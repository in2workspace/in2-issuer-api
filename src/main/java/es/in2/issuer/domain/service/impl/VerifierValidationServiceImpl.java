package es.in2.issuer.domain.service.impl;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.JWTVerificationException;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.domain.service.VerifierValidationService;
import es.in2.issuer.infrastructure.config.VerifierConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerifierValidationServiceImpl implements VerifierValidationService {

    private final VerifierConfig verifierConfig;
    private final JWTService jwtService;


    @Override
    public Mono<Void> verifyToken(String accessToken) {
        return Mono.fromCallable(() -> JWSObject.parse(accessToken))
                .flatMap(jwtService::validateJwtSignatureReactive)
                .flatMap(isValid -> {
                    String issuerDidKey = extractIssuerFromToken(accessToken);
                    Long expirationEpoch = jwtService.getExpirationFromToken(accessToken);
                    Instant expirationInstant = Instant.ofEpochSecond(expirationEpoch);
                    if (Boolean.TRUE.equals(isValid)
                            && verifierConfig.getVerifierDidKey().equals(issuerDidKey)
                            && expirationInstant.isAfter(Instant.now())) {
                        log.info("M2MTokenServiceImpl -- verifyM2MToken -- IS VALID ?: {}", true);
                        return Mono.empty();
                    } else {
                        return Mono.error(new JWTVerificationException("Token is invalid"));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error while verifying M2M token", e);
                    return Mono.error(e);
                }).then();
    }

    private String extractIssuerFromToken(String token) {
        SignedJWT signedToken = jwtService.parseJWT(token);
        Payload payload = jwtService.getPayloadFromSignedJWT(signedToken);
        return jwtService.getClaimFromPayload(payload,"iss");
    }

}
