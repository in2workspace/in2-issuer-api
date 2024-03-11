package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.SignatureRequest;
import es.in2.issuer.domain.model.SignedData;
import reactor.core.publisher.Mono;

public interface RemoteSignatureService {
    Mono<SignedData> sign(SignatureRequest signatureRequest, String token);
}
