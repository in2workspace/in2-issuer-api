package es.in2.issuer.domain.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.JWKSetParsingException;
import es.in2.issuer.domain.exception.JWTVerificationException;
import es.in2.issuer.domain.exception.TokenFetchException;
import es.in2.issuer.domain.exception.WellKnownInfoFetchException;
import es.in2.issuer.domain.model.dto.OpenIDProviderMetadata;
import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import es.in2.issuer.domain.service.VerifierService;
import es.in2.issuer.infrastructure.config.VerifierConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

import static es.in2.issuer.domain.util.Constants.CONTENT_TYPE;
import static es.in2.issuer.domain.util.Constants.CONTENT_TYPE_URL_ENCODED_FORM;
import static es.in2.issuer.domain.util.EndpointsConstants.PUBLIC_DISCOVERY_AUTH_SERVER;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerifierServiceImpl implements VerifierService {

    private final VerifierConfig verifierConfig;
    private final WebClient oauth2VerifierWebClient;

    // Cache to store JWKs and avoid multiple endpoint calls
    private volatile JWKSet cachedJWKSet;
    private final Object jwkLock = new Object();

    @Override
    public Mono<Void> verifyToken(String accessToken) {
        return parseAndValidateJwt(accessToken)
                .doOnSuccess(unused -> log.info("VerifyToken -- IS VALID"))
                .onErrorResume(e -> {
                    log.error("Error while verifying token", e);
                    return Mono.error(e);
                });
    }

    private Mono<Void> parseAndValidateJwt(String accessToken) {
        return getWellKnownInfo()
                .flatMap(metadata -> fetchJWKSet(metadata.jwksUri()))
                .flatMap(jwkSet -> {
                    try {
                        SignedJWT signedJWT = SignedJWT.parse(accessToken);

                        // Validate the issuer and expiration time
                        JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

                        String issuer = claims.getIssuer();

                        if (!verifierConfig.getVerifierExternalDomain().equals(issuer)) {
                            return Mono.error(new JWTVerificationException("Invalid issuer"));
                        }

                        Date expiration = claims.getExpirationTime();
                        if (expiration == null || expiration.toInstant().isBefore(Instant.now())) {
                            return Mono.error(new JWTVerificationException("Token has expired"));
                        }

                        // Verify the signature
                        JWSVerifier verifier = getJWSVerifier(signedJWT, jwkSet);
                        boolean isSignatureValid = signedJWT.verify(verifier);

                        if (!isSignatureValid) {
                            return Mono.error(new JWTVerificationException("Invalid token signature"));
                        }

                        return Mono.empty(); // Valid token
                    } catch (ParseException | JOSEException e) {
                        log.error("Error parsing or verifying JWT", e);
                        return Mono.error(new JWTVerificationException("Error parsing or verifying JWT"));
                    }
                });
    }

    /**
     * Creates an appropriate JWSVerifier based on the type of JWK.
     *
     * @param signedJWT The signed JWT.
     * @param jwkSet    The set of JWKs.
     * @return An appropriate JWSVerifier.
     * @throws JOSEException If an error occurs while creating the verifier.
     */
    private JWSVerifier getJWSVerifier(SignedJWT signedJWT, JWKSet jwkSet) throws JOSEException {
        String keyId = signedJWT.getHeader().getKeyID();
        JWK jwk = jwkSet.getKeyByKeyId(keyId);
        if (jwk == null) {
            throw new JOSEException("No matching JWK found for Key ID: " + keyId);
        }

        // Determine the key type and create the corresponding verifier
        if (jwk instanceof RSAKey rsaKey) {
            return new RSASSAVerifier(rsaKey.toRSAPublicKey());
        } else if (jwk instanceof ECKey ecKey) {
            return new ECDSAVerifier(ecKey.toECPublicKey());
        } else if (jwk instanceof OctetSequenceKey octKey) {
            // For symmetric keys (HS256, HS384, HS512)
            return new MACVerifier(octKey.toByteArray());
        } else {
            throw new JOSEException("Unsupported JWK type: " + jwk.getKeyType());
        }
    }

    /**
     * Retrieves the JWK set from the provided URI, using cache for optimization.
     *
     * @param jwksUri The URI where the JWK Set is located.
     * @return A Mono that emits the JWKSet.
     */
    private Mono<JWKSet> fetchJWKSet(String jwksUri) {
        if (cachedJWKSet != null) {
            return Mono.just(cachedJWKSet);
        }

        synchronized (jwkLock) {
            if (cachedJWKSet != null) {
                return Mono.just(cachedJWKSet);
            }
            return oauth2VerifierWebClient.get()
                    .uri(jwksUri)
                    .retrieve()
                    .bodyToMono(String.class)
                    .<JWKSet>handle((jwks, sink) -> {
                        try {
                            cachedJWKSet = JWKSet.parse(jwks);
                            sink.next(cachedJWKSet);
                        } catch (ParseException e) {
                            sink.error(new JWKSetParsingException("Error parsing the JWK Set"));
                        }
                    })
                    .onErrorMap(e -> new JWTVerificationException("Error fetching the JWK Set"));
        }
    }

    @Override
    public Mono<OpenIDProviderMetadata> getWellKnownInfo() {
        String wellKnownInfoEndpoint = verifierConfig.getVerifierExternalDomain() + PUBLIC_DISCOVERY_AUTH_SERVER;

        return oauth2VerifierWebClient.get()
                .uri(wellKnownInfoEndpoint)
                .retrieve()
                .bodyToMono(OpenIDProviderMetadata.class)
                .onErrorResume(e -> Mono.error(new WellKnownInfoFetchException("Error fetching OpenID Provider Metadata", e)));
    }

    @Override
    public Mono<VerifierOauth2AccessToken> performTokenRequest(String body) {
        return getWellKnownInfo()
                .flatMap(metadata -> {
                    String tokenEndpoint = metadata.tokenEndpoint();
                    return oauth2VerifierWebClient.post()
                            .uri(tokenEndpoint)
                            .header(CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED_FORM)
                            .bodyValue(body)
                            .retrieve()
                            .bodyToMono(VerifierOauth2AccessToken.class)
                            .onErrorMap(e -> new TokenFetchException("Error fetching the token", e));
                });
    }

}

