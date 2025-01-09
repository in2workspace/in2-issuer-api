package es.in2.issuer.infrastructure.crypto;

import com.nimbusds.jose.crypto.bc.BouncyCastleProviderSingleton;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import es.in2.issuer.domain.util.UVarInt;
import org.bitcoinj.base.Base58;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CryptoComponentTest {

    @Mock
    private CryptoConfig cryptoConfig;

    @InjectMocks
    private CryptoComponent cryptoComponent;

    @Test
    void testGetECKey_withGeneratedKey() throws Exception {
        // Generate an ECKey and did:key
        ECKey originalECKey = generateECKeyAndDidKey();

        // Extract the private key as hexadecimal string
        BigInteger privateKeyInt = originalECKey.toECPrivateKey().getS();
        String privateKeyHex = privateKeyInt.toString(16);

        // Configure cryptoConfig to return this private key
        when(cryptoConfig.getPrivateKey()).thenReturn(privateKeyHex);

        ECKey ecKey = cryptoComponent.getECKey();

        // Assert that the generated ECKey is the same as the one generated by the method
        assertNotNull(ecKey);
        assertEquals(originalECKey.getCurve(), ecKey.getCurve());
        assertEquals(originalECKey.getD(), ecKey.getD());
        assertEquals(originalECKey.getX(), ecKey.getX());
        assertEquals(originalECKey.getY(), ecKey.getY());
        assertEquals(originalECKey.getKeyID(), ecKey.getKeyID());
    }

    private ECKey generateECKeyAndDidKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        keyPairGenerator.initialize(256);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
        ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();
        byte[] encodedKey = publicKey.getEncoded();
        KeyFactory keyFactory = KeyFactory.getInstance("EC", BouncyCastleProviderSingleton.getInstance());
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encodedKey);
        org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey bcPublicKey = (org.bouncycastle.jcajce.provider.asymmetric.ec.BCECPublicKey) keyFactory.generatePublic(keySpec);
        byte[] pubKeyBytes = bcPublicKey.getQ().getEncoded(true);
        int multiCodecKeyCodeForSecp256r1 = 0x1200;
        UVarInt codeVarInt = new UVarInt(multiCodecKeyCodeForSecp256r1);
        int totalLength = pubKeyBytes.length + codeVarInt.getLength();
        byte[] multicodecAndRawKey = new byte[totalLength];
        System.arraycopy(codeVarInt.getBytes(), 0, multicodecAndRawKey, 0, codeVarInt.getLength());
        System.arraycopy(pubKeyBytes, 0, multicodecAndRawKey, codeVarInt.getLength(), pubKeyBytes.length);
        String multiBase58Btc = Base58.encode(multicodecAndRawKey);
        String didKey = "did:key:z" + multiBase58Btc;
        System.out.println("DID Key: " + didKey);
        return new ECKey.Builder(Curve.P_256, publicKey)
                .privateKey(privateKey)
                .keyID(didKey)
                .build();
    }
}


