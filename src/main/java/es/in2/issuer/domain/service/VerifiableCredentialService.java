package es.in2.issuer.domain.service;

import es.in2.issuer.domain.model.LEARCredentialRequest;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface VerifiableCredentialService {
    Mono<String> generateVc(String processId, String vcType, LEARCredentialRequest learCredentialRequest);
    Mono<String> generateVcPayLoad(String vcTemplate, String subjectDid, String issuerDid, String userData, Instant expiration);
    Mono<String> generateDeferredVcPayLoad(String vc);
    Mono<String> bindTheUserDidToHisCredential(String vcTemplate, String subjectDid, String issuerDid, String userData, Instant expiration);
}
