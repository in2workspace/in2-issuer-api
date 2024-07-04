package es.in2.issuer.application.workflow.impl;

import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.application.workflow.DeferredCredentialWorkflow;
import es.in2.issuer.domain.model.dto.CredentialDetails;
import es.in2.issuer.domain.model.dto.SignatureConfiguration;
import es.in2.issuer.domain.model.dto.SignatureRequest;
import es.in2.issuer.domain.model.dto.SignedCredentials;
import es.in2.issuer.domain.service.AccessTokenService;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.service.RemoteSignatureService;
import es.in2.issuer.domain.util.Constants;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.Console;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CredentialSignerWorkflowImpl implements CredentialSignerWorkflow {

    private final DeferredCredentialWorkflow deferredCredentialWorkflow;
    private final CredentialProcedureService credentialProcedureService;
    private final AccessTokenService accessTokenService;
    private final VerifiableCredentialIssuanceWorkflowImpl verifiableCredentialIssuanceWorkflow;

    @Override
    public Mono<Void> signCredential(String authorizationHeader, String procedureId) {
        return getCredential(authorizationHeader, procedureId)
                .flatMap(unsignedCredential -> getSignedCredential(unsignedCredential, authorizationHeader))
                .flatMap(this::updateSignedCredential);
    }

    private @NotNull Mono<String> getCredential(String authorizationHeader, String procedureId) {
        return accessTokenService.getOrganizationId(authorizationHeader)
                .flatMap(organizationId -> credentialProcedureService.getProcedureDetailByProcedureIdAndOrganizationId(organizationId, procedureId))
                .map(credentialDetails -> credentialDetails.credential().toString());
    }

    private @NotNull Mono<String> getSignedCredential(String unsignedCredential, String token) {
        String userId = "";
        UUID credentialId = null;
        return verifiableCredentialIssuanceWorkflow.signCredentialOnRequestedFormat(unsignedCredential, Constants.JWT_VC, userId, credentialId, token);
    }

    private Mono<Void> updateSignedCredential(String signedCredential) {
        List<SignedCredentials.SignedCredential> credentials = List.of(SignedCredentials.SignedCredential.builder().credential(signedCredential).build());
        SignedCredentials signedCredentials = new SignedCredentials(credentials);
        return deferredCredentialWorkflow.updateSignedCredentials(signedCredentials);
    }
}
