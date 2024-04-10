package es.in2.issuer.domain.service.impl;

import com.nimbusds.jose.JWSObject;
import es.in2.issuer.domain.exception.ExpiredCacheException;
import es.in2.issuer.domain.model.CustomCredentialOffer;
import es.in2.issuer.domain.service.ProofValidationService;
import es.in2.issuer.infrastructure.repository.CacheStore;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;


import org.bouncycastle.math.ec.ECCurve;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.*;
import java.text.ParseException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import io.ipfs.multibase.Base58;
import java.security.KeyFactory;

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
                if(!validateJwtBC(jwtProof, decodeDidKeyV2(encodedPublicKey))){
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
//    public static boolean validateJwt(String jwt, byte[] publicKeyBytes) throws Exception {
//        String[] parts = jwt.split("\\.");
//        if (parts.length != 3) {
//            throw new IllegalArgumentException("Invalid JWT format.");
//        }
//
//        String headerEncoded = parts[0];
//        String payloadEncoded = parts[1];
//        String signatureEncoded = parts[2];
//
//        String signedContent = headerEncoded + "." + payloadEncoded;
//        byte[] signature = Base64.getUrlDecoder().decode(signatureEncoded);
//
//        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
//        parameters.init(new ECGenParameterSpec("secp256r1"));
//        ECParameterSpec ecParameters = parameters.getParameterSpec(ECParameterSpec.class);
//
//        BigInteger x = new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 0, 32));
//        BigInteger y = new BigInteger(1, Arrays.copyOfRange(publicKeyBytes, 32, 64));
//        ECPoint point = new ECPoint(x, y);
//
//        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, ecParameters);
//        PublicKey publicKey = KeyFactory.getInstance("EC").generatePublic(pubKeySpec);
//
//        Signature sig = Signature.getInstance("SHA256withECDSA");
//        sig.initVerify(publicKey);
//        sig.update(signedContent.getBytes());
//
//        return sig.verify(signature);
//    }
    public static boolean validateJwtBC(String jwt, byte[] publicKeyBytes) throws Exception {
        try{
            // Split the JWT into its components
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format.");
            }

            String headerEncoded = parts[0];
            String payloadEncoded = parts[1];
            String signatureEncoded = parts[2];

            // Concatenate header and payload to form the signed content
            String signedContent = headerEncoded + "." + payloadEncoded;
            byte[] signature = Base64.getUrlDecoder().decode(signatureEncoded);

            // Retrieve the curve and decode the point
            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256r1");
            ECPoint point = spec.getCurve().decodePoint(publicKeyBytes);

            // Create the public key spec with the ECPoint and curve specification
            ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, spec);

            // Generate the PublicKey object
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", new BouncyCastleProvider());
            PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);

            // Initialize signature verification
            Signature sig = Signature.getInstance("SHA256withECDSA", new BouncyCastleProvider());
            sig.initVerify(publicKey);
            sig.update(signedContent.getBytes());

            // Verify the signature
            return sig.verify(signature);
        } catch (Exception e) {
            e.printStackTrace();
            // If there's any exception, the JWT validation failed
            return false;
        }
    }

    private static boolean verifyJwtSignatureV2(String jwt, byte[] publicKeyBytes) {
        try {
            // Convert the public key bytes to a PublicKey instance
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            // Prepare the JWT parser with the signing key
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build();

            // Parse and validate the JWT. This will throw an exception if the JWT is not valid.
            Jws<Claims> claims = parser.parseClaimsJws(jwt);

            // If no exception was thrown, then the JWT is valid
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            // If there's any exception, the JWT validation failed
            return false;
        }
    }



    public static byte[] decodeDidKeyV2(String didKey) throws Exception {
        // Step 1: Remove the prefix "z" to get the multibase encoded string
        if (!didKey.startsWith("z")) {
            throw new IllegalArgumentException("Invalid DID format.");
        }
        String multibaseEncoded = didKey.substring(1);

        // Step 2: Multibase decode (Base58) the encoded part to get the bytes
        byte[] decodedBytes = Base58.decode(multibaseEncoded);

        // Step 3: Assume the multicodec prefix is fixed for "0x1200"
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
