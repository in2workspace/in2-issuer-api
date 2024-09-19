package es.in2.issuer.application.workflow.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.application.workflow.DeferredCredentialWorkflow;
import es.in2.issuer.domain.exception.CredentialTypeUnsupportedException;
import es.in2.issuer.domain.model.dto.PendingCredentials;
import es.in2.issuer.domain.model.dto.SignedCredentials;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static es.in2.issuer.domain.util.Constants.*;

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
                .flatMap(this::processSignedCredential)
                .onErrorResume(e -> Mono.error(new RuntimeException("Failed to process signed credential", e)))
                .then();
    }

    private Mono<Void> processSignedCredential(SignedCredentials.SignedCredential signedCredential) {
        try {
            // Extract JWT payload
            String jwt = signedCredential.credential();
            SignedJWT signedJWT = SignedJWT.parse(jwt);
            String payload = signedJWT.getPayload().toString();

            // Parse the credential and extract the ID
            JsonNode credentialNode = objectMapper.readTree(payload);
            String credentialId = credentialNode.get("vc").get("id").asText();

            JsonNode types = credentialNode.get("vc").get("type");

            return credentialProcedureService.updatedEncodedCredentialByCredentialId(jwt, credentialId)
                    .flatMap(procedureId -> deferredCredentialMetadataService.updateVcByProcedureId(jwt, procedureId)
                            .then(deferredCredentialMetadataService.getOperationModeByProcedureId(procedureId)))
                    .flatMap(operationMode -> {
                        if (operationMode.equals(ASYNC) && types != null && types.isArray()) {
                            return sendEmailNotification(types, credentialNode);
                        }
                        return Mono.empty();
                    });
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to process signed credential", e));
        }
    }

    private Mono<Void> sendEmailNotification(JsonNode types, JsonNode credentialNode) {
        return Flux.fromIterable(types)
                .flatMap(type -> {
                    if (type.asText().equals(LEAR_CREDENTIAL_EMPLOYEE)) {
                        String email = credentialNode.get("vc").get("credentialSubject").get("mandate").get("mandatee").get("email").asText();
                        String name = credentialNode.get("vc").get("credentialSubject").get("mandate").get("mandatee").get("first_name").asText();
                        return emailService.sendCredentialSignedNotification(email, "Credential Ready", name);
                    }
                    //todo comentado hasta cuando implementemos la obtención de la VerifiableCertification en el wallet
//                    else if (type.asText().equals(VERIFIABLE_CERTIFICATION)) {
//                        String email = credentialNode.get("vc").get("credentialSubject").get("company").get("email").asText();
//                        String name = credentialNode.get("vc").get("credentialSubject").get("company").get("commonName").asText();
//                        return emailService.sendCredentialSignedNotification(email, "Credential Ready", name);
//                    }
                    else {
                        return Mono.empty();
                    }
                }).then();
    }

}
