package es.in2.issuer.domain.service;

public interface HashGeneratorService {
    String generateHash(String unsignedDocument, String algorithm);
    String generateSHA256(String unsignedDocument);
}
