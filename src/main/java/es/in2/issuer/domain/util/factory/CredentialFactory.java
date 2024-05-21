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
    public Mono<CredentialProcedureCreationRequest> getInitialCredential(String processId, String credentialType, JsonNode credential){
        switch (credentialType) {
            case LEAR_CREDENTIAL_EMPLOYEE: {
                return learCredentialEmployeeFactory.mapAndBuildLEARCredentialEmployee(credential)
                        .doOnSuccess(learCredentialEmployee -> log.info("ProcessID: {} - Credential Issued: {}", processId, credential));
            }
            default:{
                return Mono.error(new CredentialTypeUnsupportedException(credentialType));
            }
        }
    }
}
