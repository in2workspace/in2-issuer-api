package es.in2.issuer.domain.service.impl;

import com.nimbusds.jose.JWSObject;
import es.in2.issuer.domain.exception.ExpiredCacheException;
import es.in2.issuer.domain.service.ProofValidationService;
import es.in2.issuer.infrastructure.repository.CacheStore;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.PublicKey;
import java.text.ParseException;
import java.time.Instant;
import java.util.Map;

import io.ipfs.multibase.Base58;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;

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
                // Validate nonce
                if(Boolean.FALSE.equals(isNoncePresentInCache(nonce).block())){
                    return false;
                }

                // Validate signature
//                if(!verifyJwtSignature(jwtProof, decodeDidKey(encodedPublicKey))){
//                    return false;
//                }

                return true; // All checks passed
            } catch (ParseException e) {
                // Handle parsing exception
                return false;
            }
        });
    }

    private static boolean verifyJwtSignature(String jwtString, PublicKey publicKey) {
        log.info("entro al verifyJwtSignature");
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(jwtString);
            // If parsing is successful without exceptions, signature is valid
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private PublicKey decodeDidPublicKey(String didKey) throws Exception {
        // Step 1: Strip the identifier
        String base58Encoded = didKey.substring(1);

        // Step 2: Decode from Base58
        byte[] decodedBytes = Base58.decode(base58Encoded);

        // Step 3: Strip the Multicodec Prefix
        byte[] publicKeyBytes = new byte[decodedBytes.length - 2];
        System.arraycopy(decodedBytes, 2, publicKeyBytes, 0, publicKeyBytes.length);

        // Step 4: Construct the PublicKey Object
        X509EncodedKeySpec spec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("EC");
        return keyFactory.generatePublic(spec);
    }



    public PublicKey decodeDidKey(String didKey) throws Exception {
        // Remove the did:key:z prefix and decode from Base58
        String base58EncodedKey = didKey.substring(1);
        byte[] decodedBytes = Base58.decode(base58EncodedKey);

        // Extract the raw public key bytes
        byte[] publicKeyBytes = new byte[decodedBytes.length - 2];
        System.arraycopy(decodedBytes, 2, publicKeyBytes, 0, publicKeyBytes.length);

        // Convert the raw public key bytes back into a PublicKey object
        KeyFactory keyFactory = KeyFactory.getInstance("EC", new BouncyCastleProvider());
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
        return publicKey;
    }

    private Boolean isNonceInCache(String key) {
            String nonceInCache = String.valueOf(cacheStore.get(key));
            log.info(nonceInCache);
            if (nonceInCache != null) {
                return true;
            }
            return false;
    }

    private Mono<Boolean> isNoncePresentInCache(String nonce) {
        return Mono.fromCallable(() -> cacheStore.get(nonce) != null);
    }
}
