package es.in2.issuer.backoffice.domain.service;


import es.in2.issuer.backoffice.domain.model.dto.SignatureRequest;
import es.in2.issuer.backoffice.domain.model.dto.SignedData;
import reactor.core.publisher.Mono;

public interface RemoteSignatureService {
    Mono<SignedData> sign(SignatureRequest signatureRequest, String token, String procedureId);
}
