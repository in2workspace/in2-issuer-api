package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.databind.JsonNode;
import es.in2.issuer.domain.exception.CredentialTypeUnsupportedException;
import es.in2.issuer.domain.model.CredentialProcedureCreationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL_EMPLOYEE;

@Component
@RequiredArgsConstructor
@Slf4j
public class CredentialFactory {
    public final LEARCredentialEmployeeFactory learCredentialEmployeeFactory;
    public Mono<CredentialProcedureCreationRequest> mapCredentialIntoACredentialProcedureRequest(String processId, String credentialType, JsonNode credential){
        if (credentialType.equals(LEAR_CREDENTIAL_EMPLOYEE)) {
            return learCredentialEmployeeFactory.mapAndBuildLEARCredentialEmployee(credential)
                    .doOnSuccess(learCredentialEmployee -> log.info("ProcessID: {} - Credential mapped: {}", processId, credential));
        }
        return Mono.error(new CredentialTypeUnsupportedException(credentialType));
    }
    public Mono<String> mapCredentialAndBindMandateeId(String processId, String credentialType, String credential, String mandateeId){
        if (credentialType.equals(LEAR_CREDENTIAL_EMPLOYEE)) {
            return learCredentialEmployeeFactory.mapCredentialAndBindMandateeIdInToTheCredential(credential, mandateeId)
                    .doOnSuccess(learCredentialEmployee -> log.info("ProcessID: {} - Credential mapped and bind to the id: {}", processId, credential));
        }
        return Mono.error(new CredentialTypeUnsupportedException(credentialType));
    }



}
