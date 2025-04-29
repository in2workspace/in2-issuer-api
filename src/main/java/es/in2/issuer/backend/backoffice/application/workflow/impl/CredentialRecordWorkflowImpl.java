package es.in2.issuer.backend.backoffice.application.workflow.impl;


import es.in2.issuer.backend.backoffice.application.workflow.CredentialRecordWorkflow;
import es.in2.issuer.backend.shared.application.workflow.CredentialIssuanceWorkflow;
import es.in2.issuer.backend.shared.domain.model.dto.CredentialDetails;
import es.in2.issuer.backend.shared.domain.model.dto.CredentialProcedures;
import es.in2.issuer.backend.shared.domain.model.dto.PreSubmittedCredentialRequest;
import es.in2.issuer.backend.shared.domain.service.AccessTokenService;
import es.in2.issuer.backend.shared.domain.service.CredentialProcedureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CredentialRecordWorkflowImpl implements CredentialRecordWorkflow {

    private final CredentialProcedureService credentialProcedureService;
    private final AccessTokenService accessTokenService;
    private final CredentialIssuanceWorkflow credentialIssuanceWorkflow;

    @Override
    public Mono<Void> createCredentialRecord(String processId,
                                             PreSubmittedCredentialRequest preSubmittedCredentialRequest,
                                             String authorizationHeader) {
        return accessTokenService.getCleanBearerToken(authorizationHeader).flatMap(
                token -> credentialIssuanceWorkflow
                        .execute(processId, preSubmittedCredentialRequest, authorizationHeader, null));

    }

    @Override
    public Mono<CredentialProcedures> getAllCredentialRecords(String processId, String authorizationHeader) {
        return accessTokenService.getOrganizationId(authorizationHeader)
                .flatMap(credentialProcedureService::getAllProceduresBasicInfoByOrganizationId);

    }

    @Override
    public Mono<CredentialDetails> getCredentialRecordById(String processId, String recordId, String authorizationHeader) {
        return accessTokenService.getOrganizationId(authorizationHeader)
                .flatMap(organizationId ->
                        credentialProcedureService.getProcedureDetailByProcedureIdAndOrganizationId(organizationId, recordId)
                );

    }

}
