package es.in2.issuer.backend.domain.util.factory;

import com.fasterxml.jackson.databind.JsonNode;
import es.in2.issuer.backend.domain.exception.CredentialTypeUnsupportedException;
import es.in2.issuer.backend.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.backend.domain.model.dto.PreSubmittedCredentialRequest;
import es.in2.issuer.backend.domain.service.CredentialProcedureService;
import es.in2.issuer.backend.domain.service.DeferredCredentialMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static es.in2.issuer.shared.domain.util.Constants.LEAR_CREDENTIAL_EMPLOYEE;
import static es.in2.issuer.backend.domain.util.Constants.VERIFIABLE_CERTIFICATION;

@Component
@RequiredArgsConstructor
@Slf4j
public class CredentialFactory {

    public final LEARCredentialEmployeeFactory learCredentialEmployeeFactory;
    public final LEARCredentialMachineFactory learCredentialMachineFactory;
    public final VerifiableCertificationFactory verifiableCertificationFactory;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;

    public Mono<CredentialProcedureCreationRequest> mapCredentialIntoACredentialProcedureRequest(String processId, String credentialType, PreSubmittedCredentialRequest preSubmittedCredentialRequest, String token) {
        JsonNode credential = preSubmittedCredentialRequest.payload();
        String operationMode = preSubmittedCredentialRequest.operationMode();
        if (credentialType.equals(LEAR_CREDENTIAL_EMPLOYEE)) {
            return learCredentialEmployeeFactory.mapAndBuildLEARCredentialEmployee(credential, operationMode)
                    .doOnSuccess(learCredentialEmployee -> log.info("ProcessID: {} - LEARCredentialEmployee mapped: {}", processId, credential));
        } else if (credentialType.equals(VERIFIABLE_CERTIFICATION)) {
            return verifiableCertificationFactory.mapAndBuildVerifiableCertification(credential, token, operationMode)
                    .doOnSuccess(verifiableCertification -> log.info("ProcessID: {} - VerifiableCertification mapped: {}", processId, credential));
        }
        return Mono.error(new CredentialTypeUnsupportedException(credentialType));
    }
    public Mono<String> mapCredentialAndBindMandateeId(String processId, String credentialType, String decodedCredential, String mandateeId){
        if (credentialType.equals(LEAR_CREDENTIAL_EMPLOYEE)) {
            return learCredentialEmployeeFactory.mapCredentialAndBindMandateeIdInToTheCredential(decodedCredential, mandateeId)
                    .doOnSuccess(learCredentialEmployee -> log.info("ProcessID: {} - Credential mapped and bind to the id: {}", processId, learCredentialEmployee));
        }
        return Mono.error(new CredentialTypeUnsupportedException(credentialType));
    }

    public Mono<Void> mapCredentialBindIssuerAndUpdateDB(String processId, String procedureId, String decodedCredential, String credentialType, String format, String authServerNonce) {
        if (credentialType.equals(LEAR_CREDENTIAL_EMPLOYEE)) {
            return learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(decodedCredential, procedureId)
                    .flatMap(bindCredential -> {
                        log.info("ProcessID: {} - Credential mapped and bind to the issuer: {}", processId, bindCredential);
                        return credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindCredential, format)
                                .then(deferredCredentialMetadataService.updateDeferredCredentialByAuthServerNonce(authServerNonce, format));
                    });
        }
        return Mono.error(new CredentialTypeUnsupportedException(credentialType));
    }
}
