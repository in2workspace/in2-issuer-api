package es.in2.issuer.application.service;

import es.in2.issuer.domain.model.LEARCredentialRequest;
import reactor.core.publisher.Mono;

public interface LEARCredentialWithdrawService {
    Mono<Void> completeWithdrawLearCredentialProcess(String processId, LEARCredentialRequest learCredentialRequest);
}
