package es.in2.issuer.infrastructure.crypto;

import es.in2.issuer.infrastructure.config.properties.VerifierProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CryptoConfig {

    private final VerifierProperties verifierProperties;

    public String getPrivateKey() {
        String privateKey = verifierProperties.crypto().privateKey();
        if (privateKey.startsWith("0x")) {
            privateKey = privateKey.substring(2);
        }
        return privateKey;
    }

}