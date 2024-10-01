package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.exception.PublicKeyDecodingException;
import es.in2.issuer.domain.exception.UnsupportedDIDTypeException;
import es.in2.issuer.domain.service.DIDService;
import io.github.novacrypto.base58.Base58;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.custom.sec.SecP256R1Curve;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class DIDServiceImpl implements DIDService {

    @Override
    public PublicKey getPublicKeyFromDid(String did) {
        if (!did.startsWith("did:key:")) {
            throw new UnsupportedDIDTypeException("Unsupported DID type. Only did:key is supported for the moment.");
        }

        // Remove the "did:key:" prefix to get the actual encoded public key
        String encodedPublicKey = did.substring("did:key:".length());

        // Decode the public key from its encoded representation
        return decodePublicKeyIntoPubKey(encodedPublicKey);
    }

    private PublicKey decodePublicKeyIntoPubKey(String encodePublicKey) {
        try {
            // Remove the prefix "z" to get the multibase encoded string
            if (!encodePublicKey.startsWith("z")) {
                throw new PublicKeyDecodingException("Invalid Public Key.");
            }
            String multibaseEncoded = encodePublicKey.substring(1);

            // Multibase decode (Base58) the encoded part to get the bytes
            byte[] decodedBytes = Base58.base58Decode(multibaseEncoded);

            // Multicodec prefix is fixed for "0x1200" for the secp256r1 curve
            int prefixLength = 2;

            // Extract public key bytes after the multicodec prefix
            byte[] publicKeyBytes = new byte[decodedBytes.length - prefixLength];
            System.arraycopy(decodedBytes, prefixLength, publicKeyBytes, 0, publicKeyBytes.length);

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
            return kf.generatePublic(pubKeySpec);
        }
        catch (Exception e) {
            throw new PublicKeyDecodingException("JWT signature verification failed.", e);
        }
    }

}
