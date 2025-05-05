package es.in2.issuer.backend.shared.domain.service;

import es.in2.issuer.backend.shared.domain.exception.HashGenerationException;

public interface HashGeneratorService {
    String generateHash(String unsignedDocument, String algorithm) throws HashGenerationException;
    String generateSHA256(String unsignedDocument) throws HashGenerationException;
}
