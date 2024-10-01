package es.in2.issuer.infrastructure.crypto;

import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import es.in2.issuer.domain.exception.ECKeyCreationException;
import es.in2.issuer.domain.util.UVarInt;
import lombok.RequiredArgsConstructor;
import org.bitcoinj.base.Base58;
import org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;

@Configuration
@RequiredArgsConstructor
public class CryptoComponent {

    private final CryptoConfig cryptoConfig;

    @Bean
    public ECKey getECKey() {
        return buildEcKeyFromPrivateKey();
    }

    /**
     * Documentation: <a href="https://connect2id.com/products/nimbus-jose-jwt/examples/jwt-with-es256r-signature">JSON Web Token (JWT) with ES256K (secp256k1) signature</a>
     *
     * @return - ECKey
     */
    private ECKey buildEcKeyFromPrivateKey() {
        try {
            // Convert the private key from hexadecimal string to BigInteger
            BigInteger privateKeyInt = new BigInteger(cryptoConfig.getPrivateKey(), 16);

            // Get the curve parameters for secp256r1 (P-256)
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");

            // Initialize the key factory for EC algorithm
            KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProviderSingleton.getInstance());

            // Create the private key spec for secp256r1
            ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(privateKeyInt, ecSpec);
            ECPrivateKey privateKey = (ECPrivateKey) keyFactory.generatePrivate(privateKeySpec);

            // Generate the public key spec from the private key and curve parameters
            ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(ecSpec.getG().multiply(privateKeyInt), ecSpec);
            ECPublicKey publicKey = (ECPublicKey) keyFactory.generatePublic(publicKeySpec);

            // Build the ECKey using secp256r1 curve (P-256)
            return new ECKey.Builder(Curve.P_256, publicKey)
                    .privateKey(privateKey)
                    .keyID(generateDidKey(publicKey))
                    .build();
        } catch (Exception e) {
            throw new ECKeyCreationException("Error creating JWK source for secp256r1: " + e);
        }
    }


    // Generates a standard DID Key
    private String generateDidKey(ECPublicKey ecPublicKey){
        try {
            // Convert ECPublicKey (Java) to BCECPublicKey (Bouncy Castle)
            byte[] encodedKey = ecPublicKey.getEncoded();
            KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProviderSingleton.getInstance());

            // Decode the public key using Bouncy Castle
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
            BCECPublicKey bcPublicKey = (BCECPublicKey) keyFactory.generatePublic(keySpec);

            // Extract the bytes from BCECPublicKey
            byte[] pubKeyBytes = bcPublicKey.getQ().getEncoded(true);

            // Multicodec key code for secp256r1
            int multiCodecKeyCodeForSecp256r1 = 0x1200;

            // Convert the public key to multibase58 format
            String multiBase58Btc = convertRawKeyToMultiBase58Btc(pubKeyBytes, multiCodecKeyCodeForSecp256r1);
            return "did:key:z" + multiBase58Btc;

        } catch (Exception e) {
            throw new RuntimeException("Error converting public key to did:key", e);
        }
    }

    // Converts raw public key bytes into a multibase58 string
    private String convertRawKeyToMultiBase58Btc(byte[] publicKey, int code) {
        UVarInt codeVarInt = new UVarInt(code);

        // Calculate the total length of the resulting byte array
        int totalLength = publicKey.length + codeVarInt.getLength();

        // Create a byte array to hold the multicodec and raw key
        byte[] multicodecAndRawKey = new byte[totalLength];

        // Copy the UVarInt bytes to the beginning of the byte array
        System.arraycopy(codeVarInt.getBytes(), 0, multicodecAndRawKey, 0, codeVarInt.getLength());

        // Copy the raw public key bytes after the UVarInt bytes
        System.arraycopy(publicKey, 0, multicodecAndRawKey, codeVarInt.getLength(), publicKey.length);

        // Encode the combined byte array to Base58
        return Base58.encode(multicodecAndRawKey);
    }
}