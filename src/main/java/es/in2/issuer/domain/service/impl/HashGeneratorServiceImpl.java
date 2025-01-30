package es.in2.issuer.domain.service.impl;
import es.in2.issuer.domain.service.HashGeneratorService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class HashGeneratorServiceImpl implements  HashGeneratorService {

    @Override
    public String generateHash(String unsignedDocument, String algorithm) {
        if(algorithm.equals("2.16.840.1.101.3.4.2.1")){
            return generateSHA256(unsignedDocument);
        } else {
            throw new RuntimeException("Error generating hash: algorithm not supported");
        }
    }
    @Override
    public String generateSHA256(String unsignedDocument) {
        try {
            byte[] documentBytes = unsignedDocument.getBytes(StandardCharsets.UTF_8);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(documentBytes);
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error generating SHA-256 hash", e);
        }
    }
}
