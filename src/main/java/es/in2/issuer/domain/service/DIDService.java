package es.in2.issuer.domain.service;

import java.security.PublicKey;

public interface DIDService {
    PublicKey getPublicKeyFromDid(String did);
}
