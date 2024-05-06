package es.in2.issuer.domain.service.impl;

import com.nimbusds.jose.JWSObject;
import es.in2.issuer.domain.exception.ProofValidationException;
import es.in2.issuer.domain.service.ProofValidationService;
import es.in2.issuer.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.interfaces.ECPublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import io.github.novacrypto.base58.Base58;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.math.BigInteger;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.custom.sec.SecP256R1Curve;
import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.ECNamedCurveTable;

import static es.in2.issuer.domain.util.Constants.SUPPORTED_PROOF_ALG;
import static es.in2.issuer.domain.util.Constants.SUPPORTED_PROOF_TYP;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProofValidationServiceImpl implements ProofValidationService {

    private final CacheStore<String> cacheStore;

    @Override
    public Mono<Boolean> isProofValid(String jwtProof) {
        return Mono.just(jwtProof)
                .doOnNext(jwt -> log.debug("Starting validation for JWT: {}", jwt))
                .flatMap(this::parseAndValidateJwt)
                .doOnNext(jws -> log.debug("JWT parsed successfully"))
                .flatMap(jwsObject ->
                        validateJwtSignatureReactive(jwsObject)
                                .doOnSuccess(isSignatureValid -> log.debug("Signature validation result: {}", isSignatureValid))
                                .map(isSignatureValid -> Boolean.TRUE.equals(isSignatureValid) ? jwsObject : null)
                )
                .doOnNext(jwsObject -> {
                    if (jwsObject == null) log.debug("JWT signature validation failed");
                    else log.debug("JWT signature validated, checking nonce...");
                })
                .flatMap(jwsObject ->
                        jwsObject != null ? isNoncePresentInCache(jwsObject) : Mono.just(false)
                )
                .doOnSuccess(result -> log.debug("Final validation result: {}", result))
                .onErrorMap(e -> {
                    log.error("Error during JWT validation", e);
                    return new ProofValidationException("Error during JWT validation");
                });
    }

    private Mono<JWSObject> parseAndValidateJwt(String jwtProof) {
        return Mono.fromCallable(() -> {
            JWSObject jwsObject = JWSObject.parse(jwtProof);
            validateHeader(jwsObject);
            validatePayload(jwsObject);
            return jwsObject;
        });
    }

    private void validateHeader(JWSObject jwsObject) {
        Map<String, Object> headerParams = jwsObject.getHeader().toJSONObject();
        if (headerParams.get("alg") == null || headerParams.get("typ") == null ||
                !SUPPORTED_PROOF_ALG.equals(headerParams.get("alg")) ||
                !SUPPORTED_PROOF_TYP.equals(headerParams.get("typ"))) {
            throw new IllegalArgumentException("Invalid JWT header");
        }
    }

    private void validatePayload(JWSObject jwsObject) {
        var payload = jwsObject.getPayload().toJSONObject();
        if (!payload.containsKey("aud") || !payload.containsKey("iat") ||
                Instant.now().isAfter(Instant.ofEpochSecond(Long.parseLong(payload.get("exp").toString())))) {
            throw new IllegalArgumentException("Invalid JWT payload");
        }
    }

    private Mono<Boolean> validateJwtSignatureReactive(JWSObject jwsObject) {
        String kid = jwsObject.getHeader().getKeyID();
        String encodedPublicKey = kid.substring(kid.indexOf("#") + 1);
        return decodePublicKeyIntoBytes(encodedPublicKey)
                .flatMap(publicKeyBytes -> validateJwtSignature(jwsObject.getParsedString(), publicKeyBytes));
    }


    private Mono<Boolean> validateJwtSignature(String jwtString, byte[] publicKeyBytes) {
        return Mono.fromCallable(() -> {
            try {
                // Set the curve as secp256r1
                ECCurve curve = new SecP256R1Curve();
                BigInteger x = new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 1, publicKeyBytes.length));

                // Recover the Y coordinate from the X coordinate and the curve
                BigInteger y = curve.decodePoint(publicKeyBytes).getYCoord().toBigInteger();

                ECPoint point = new ECPoint(x, y);

                // Fetch the ECParameterSpec for secp256r1
                ECNamedCurveParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
                ECNamedCurveSpec params = new ECNamedCurveSpec("secp256r1", ecSpec.getCurve(), ecSpec.getG(), ecSpec.getN());

                // Create a KeyFactory and generate the public key
                KeyFactory kf = KeyFactory.getInstance("EC");
                ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);
                PublicKey publicKey = kf.generatePublic(pubKeySpec);

                // Parse the JWT and create a verifier
                SignedJWT signedJWT = SignedJWT.parse(jwtString);
                ECDSAVerifier verifier = new ECDSAVerifier((ECPublicKey) publicKey);

                // Verify the signature
                return signedJWT.verify(verifier);
            } catch (Exception e) {
                return false; // In case of any exception, return false
            }
        });
    }

    private Mono<byte[]> decodePublicKeyIntoBytes(String publicKey) {
        return Mono.fromCallable(() -> {
            // Remove the prefix "z" to get the multibase encoded string
            if (!publicKey.startsWith("z")) {
                throw new IllegalArgumentException("Invalid Public Key.");
            }
            String multibaseEncoded = publicKey.substring(1);

            // Multibase decode (Base58) the encoded part to get the bytes
            byte[] decodedBytes = Base58.base58Decode(multibaseEncoded);

            // Multicodec prefix is fixed for "0x1200" for the secp256r1 curve
            int prefixLength = 2;

            // Extract public key bytes after the multicodec prefix
            byte[] publicKeyBytes = new byte[decodedBytes.length - prefixLength];
            System.arraycopy(decodedBytes, prefixLength, publicKeyBytes, 0, publicKeyBytes.length);

            return publicKeyBytes;
        });
    }

    private Mono<Boolean> isNoncePresentInCache(JWSObject jwsObject) {
        // Extract nonce and check in cache
        var payload = jwsObject.getPayload().toJSONObject();
        String nonce = payload.get("nonce").toString();
        return Mono.fromCallable(() -> cacheStore.get(nonce) != null)
                .onErrorReturn(false);
    }

}
