package es.in2.issuer.backend.backoffice.application.workflow;

import es.in2.issuer.backend.shared.domain.model.dto.CredentialDetails;
import es.in2.issuer.backend.shared.domain.model.dto.CredentialProcedures;
import es.in2.issuer.backend.shared.domain.model.dto.PreSubmittedCredentialRequest;
import reactor.core.publisher.Mono;

public interface CredentialRecordWorkflow {
    Mono<Void> createCredentialRecord(String processId, PreSubmittedCredentialRequest preSubmittedCredentialRequest, String authorizationHeader);
    Mono<CredentialProcedures> getAllCredentialRecords(String processId, String authorizationHeader);
    Mono<CredentialDetails> getCredentialRecordById(String processId, String recordId, String authorizationHeader);
}
