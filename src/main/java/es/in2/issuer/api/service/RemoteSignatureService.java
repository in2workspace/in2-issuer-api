package es.in2.issuer.api.service;

import es.in2.issuer.api.model.dto.SignatureRequest;
import es.in2.issuer.api.model.dto.SignedData;
import reactor.core.publisher.Mono;

public interface RemoteSignatureService {
    Mono<SignedData> sign(SignatureRequest signatureRequest, String token);
}
