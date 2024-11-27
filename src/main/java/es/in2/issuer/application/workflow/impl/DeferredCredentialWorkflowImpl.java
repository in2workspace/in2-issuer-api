package es.in2.issuer.application.workflow.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.application.workflow.DeferredCredentialWorkflow;
import es.in2.issuer.domain.model.dto.PendingCredentials;
import es.in2.issuer.domain.model.dto.SignedCredentials;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static es.in2.issuer.domain.util.Constants.ASYNC;

@Service
@RequiredArgsConstructor
public class DeferredCredentialWorkflowImpl implements DeferredCredentialWorkflow {

    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    @Override
    public Mono<PendingCredentials> getPendingCredentialsByOrganizationId(String organizationId) {
        return credentialProcedureService.getAllIssuedCredentialByOrganizationIdentifier(organizationId)
                .map(decodedCredential -> PendingCredentials.CredentialPayload.builder()
                        .credential(decodedCredential)
                        .build())
                .collectList()
                .map(PendingCredentials::new);
    }

    @Override
    public Mono<Void> updateSignedCredentials(SignedCredentials signedCredentials) {
        return Flux.fromIterable(signedCredentials.credentials())
                .flatMap(signedCredential -> {
                    try {
                        // Extract JWT payload
                        String jwt = signedCredential.credential();
                        SignedJWT signedJWT = SignedJWT.parse(jwt);
                        String payload = signedJWT.getPayload().toString();
                        // Parse the credential and extract the ID
                        JsonNode credentialNode = objectMapper.readTree(payload);
                        String credentialId = credentialNode.get("jwtCredential").get("id").asText();
                        // Update the credential in the database
                        return credentialProcedureService.updatedEncodedCredentialByCredentialId(jwt, credentialId)
                                .flatMap(procedureId -> deferredCredentialMetadataService.updateVcByProcedureId(jwt, procedureId)
                                        // Send notification email depending on operationMode
                                        .then(deferredCredentialMetadataService.getOperationModeByProcedureId(procedureId))
                                        .flatMap(operationMode -> {
                                            if(operationMode.equals(ASYNC)){
                                                String email = credentialNode.get("jwtCredential").get("credentialSubject").get("mandate").get("mandatee").get("email").asText();
                                                String firstName = credentialNode.get("jwtCredential").get("credentialSubject").get("mandate").get("mandatee").get("first_name").asText();
                                                return emailService.sendCredentialSignedNotification(email, "Credential Ready", firstName);
                                            }
                                            return Mono.empty();
                                        })
                                );
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Failed to process signed credential", e));
                    }
                })
                .then();
    }

}
