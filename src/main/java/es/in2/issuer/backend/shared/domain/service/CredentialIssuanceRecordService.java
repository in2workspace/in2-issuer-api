package es.in2.issuer.backend.shared.domain.service;

import es.in2.issuer.backend.shared.domain.model.dto.PreSubmittedDataCredentialRequest;
import reactor.core.publisher.Mono;

public interface CredentialIssuanceRecordService {
    Mono<String> create(
            String processId,
            PreSubmittedDataCredentialRequest preSubmittedDataCredentialRequest,
            String token);
}
