package es.in2.issuer.infrastructure.crypto;

import es.in2.issuer.infrastructure.config.VerifierConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CryptoConfig {

    private final VerifierConfig verifierConfig;

    public String getPrivateKey() {
        String privateKey = verifierConfig.getVerifierCryptoPrivateKey();
        if (privateKey.startsWith("0x")) {
            privateKey = privateKey.substring(2);
        }
        return privateKey;
    }

}