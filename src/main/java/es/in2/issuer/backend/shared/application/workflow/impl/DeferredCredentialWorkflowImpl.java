package es.in2.issuer.backend.shared.application.workflow.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.backend.shared.application.workflow.DeferredCredentialWorkflow;
import es.in2.issuer.backend.shared.domain.model.dto.PendingCredentials;
import es.in2.issuer.backend.shared.domain.model.dto.SignedCredentials;
import es.in2.issuer.backend.shared.domain.service.CredentialProcedureService;
import es.in2.issuer.backend.shared.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.backend.shared.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static es.in2.issuer.backend.backoffice.domain.util.Constants.*;

@Slf4j
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
                        log.debug("Credential payload: {}", payload);
                        // Parse the credential and extract the ID
                        JsonNode credentialNode = objectMapper.readTree(payload);
                        String credentialId = credentialNode.get(VC).get("id").asText();
                        // Update the credential in the database
                        return credentialProcedureService.updatedEncodedCredentialByCredentialId(jwt, credentialId)
                                .flatMap(procedureId -> deferredCredentialMetadataService.updateVcByProcedureId(jwt, procedureId)
                                        // Send notification email depending on operationMode
                                        .then(deferredCredentialMetadataService.getOperationModeByProcedureId(procedureId))
                                        .flatMap(operationMode -> {
                                            if(operationMode.equals(ASYNC)){
                                                JsonNode vcNode = credentialNode.has(VC) ? credentialNode.get(VC) : credentialNode;
                                                JsonNode credentialSubjectNode = vcNode.path(CREDENTIAL_SUBJECT);

                                                String email = null;
                                                String firstName = null;
                                                String sentence = null;

                                                if (credentialSubjectNode.has(MANDATE) && credentialSubjectNode.get(MANDATE).has(MANDATEE)) {
                                                    JsonNode mandateeNode = credentialSubjectNode.get(MANDATE).get(MANDATEE);
                                                    email = mandateeNode.path(EMAIL).asText(null);
                                                    firstName = mandateeNode.path(FIRST_NAME).asText(null);
                                                    sentence = "You can now use it with your Wallet";
                                                }
                                                else if (credentialSubjectNode.has("company")) {
                                                    JsonNode companyNode = credentialSubjectNode.get("company");
                                                    email = companyNode.path(EMAIL).asText(null);
                                                    firstName = companyNode.path("commonName").asText(null);
                                                    sentence = "It is now ready to be applied to your product.";
                                                }

                                                if (email == null || firstName == null) {
                                                    log.error("Missing email or firstName in credential subject. Skipping email notification.");
                                                    return Mono.error(new ResponseStatusException(
                                                            HttpStatus.BAD_REQUEST,
                                                            "Missing required credentialSubject properties: email and firstName"
                                                    ));
                                                }

                                                return emailService.sendCredentialSignedNotification(email, "Credential Ready", firstName, sentence);

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
