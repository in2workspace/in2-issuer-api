package es.in2.issuer.domain.service.impl;

import com.nimbusds.jose.JWSObject;
import es.in2.issuer.domain.service.ProofValidationService;
import es.in2.issuer.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import io.ipfs.multibase.Base58;

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
        return Mono.fromCallable(() -> {
            try {
                JWSObject jwsObject = JWSObject.parse(jwtProof);
                Map<String, Object> headerParams = jwsObject.getHeader().toJSONObject();
                var payload = jwsObject.getPayload().toJSONObject();
                String kid = jwsObject.getHeader().getKeyID();
                // Extract the public key identifier from the 'kid' value
                String encodedPublicKey = kid.substring(kid.indexOf("#") + 1);
                // Extract the expiration time
                Instant expiration = Instant.ofEpochSecond(Long.parseLong(payload.get("exp").toString()));
                // Extract nonce
                String nonce = payload.get("nonce").toString();

                // Validate header
                if (jwsObject.getHeader().toJSONObject().get("alg") == null || jwsObject.getHeader().toJSONObject().get("typ") == null) {
                    return false;
                }
                // Validate specific 'alg' and 'typ' values for the proof type
                if (!SUPPORTED_PROOF_ALG.equals(headerParams.get("alg")) || !SUPPORTED_PROOF_TYP.equals(headerParams.get("typ"))) {
                    return false;
                }
                // Validate payload claims
                if (!payload.containsKey("aud") || !payload.containsKey("iat")) {
                    return false;
                }
                // Validate expiration
                if(Instant.now().isAfter(expiration)){
                    return false;
                }
                // Validate signature
                if(!validateJwtSignature(jwtProof, decodeDidKey(encodedPublicKey))){
                    return false;
                }

                // If all preliminary checks passed, proceed with nonce check
                return isNoncePresentInCache(nonce);
            } catch (ParseException e) {
                // Handle parsing exception
                return false;
            }
        }).flatMap(noncePresent -> {
                    if (Boolean.FALSE.equals(noncePresent)) {
                        // If nonce is not present in the cache, the proof is invalid
                        return Mono.just(false);
                    }
                    // If nonce is found, the proof is valid
                    return Mono.just(true);
                })
                .onErrorReturn(false); // Handle any errors by returning false
    }

    private boolean validateJwtSignature(String jwtString, byte[] publicKeyBytes) {
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
            e.printStackTrace();
            return false; // In case of any exception, return false
        }
    }

    private byte[] decodeDidKey(String didKey) throws Exception {
        // Remove the prefix "z" to get the multibase encoded string
        if (!didKey.startsWith("z")) {
            throw new IllegalArgumentException("Invalid DID format.");
        }
        String multibaseEncoded = didKey.substring(1);

        // Multibase decode (Base58) the encoded part to get the bytes
        byte[] decodedBytes = Base58.decode(multibaseEncoded);

        // Multicodec prefix is fixed for "0x1200" for the secp256r1 curve
        int prefixLength = 2;

        // Extract public key bytes after the multicodec prefix
        byte[] publicKeyBytes = new byte[decodedBytes.length - prefixLength];
        System.arraycopy(decodedBytes, prefixLength, publicKeyBytes, 0, publicKeyBytes.length);

        return publicKeyBytes;
    }

    private Mono<Boolean> isNoncePresentInCache(String nonce) {
        return Mono.fromCallable(() -> cacheStore.get(nonce) != null)
                .onErrorReturn(false);
    }

}
