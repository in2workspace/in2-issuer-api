package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.LEARCredentialEmployee;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL_EMPLOYEE;
import static es.in2.issuer.domain.util.Constants.VERIFIABLE_CREDENTIAL;

@Component
@RequiredArgsConstructor
public class LEARCredentialEmployeeFactory {
    private final ObjectMapper objectMapper;
    private final AppConfiguration appConfiguration;

    public Mono<CredentialProcedureCreationRequest> mapAndBuildLEARCredentialEmployee(JsonNode learCredential){
        LEARCredentialEmployee baseLearCredentialEmployee = mapStringToLEARCredentialEmployee(learCredential);

        return buildFinalLearCredentialEmployee(baseLearCredentialEmployee)
                .flatMap(learCredentialEmployee -> convertLEARCredentialEmployeeInToString(learCredentialEmployee)
                    .flatMap(decodedCredential -> buildCredentialProcedureCreationRequest(decodedCredential,learCredentialEmployee))
                );
    }
    private LEARCredentialEmployee mapStringToLEARCredentialEmployee(JsonNode learCredential){
        return objectMapper.convertValue(learCredential, LEARCredentialEmployee.class);
    }
    private Mono<LEARCredentialEmployee> buildFinalLearCredentialEmployee(LEARCredentialEmployee baseLearCredentialEmployee){
        Instant currentTime = Instant.now();

        return Mono.just(LEARCredentialEmployee.builder()
                .expirationDate(currentTime.plus(30, ChronoUnit.DAYS).toString())
                .issuanceDate(currentTime.toString())
                .validFrom(currentTime.toString())
                .id(UUID.randomUUID().toString())
                .type(List.of(LEAR_CREDENTIAL_EMPLOYEE,VERIFIABLE_CREDENTIAL))
                .issuer(appConfiguration.getIssuerDid())
                .credentialSubject(LEARCredentialEmployee.CredentialSubject.builder()
                        .mandate(LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                                .mandator(baseLearCredentialEmployee.credentialSubject().mandate().mandator())
                                .mandatee(baseLearCredentialEmployee.credentialSubject().mandate().mandatee())
                                .power(baseLearCredentialEmployee.credentialSubject().mandate().power())
                                .lifeSpan(LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan.builder()
                                        .startDateTime(currentTime.toString())
                                        .endDateTime(currentTime.plus(30, ChronoUnit.DAYS).toString())
                                        .build())
                                .build())
                        .build())
                .build());
    }
    private Mono<String> convertLEARCredentialEmployeeInToString(LEARCredentialEmployee learCredentialEmployee){
        try {
            return Mono.just(objectMapper.writeValueAsString(learCredentialEmployee));
        }
        catch (JsonProcessingException e){
            return Mono.error(new RuntimeException());
        }
    }


    private Mono<CredentialProcedureCreationRequest> buildCredentialProcedureCreationRequest(String decodedCredential, LEARCredentialEmployee learCredentialEmployee) {
        return Mono.just(
                CredentialProcedureCreationRequest.builder()
                        .credentialId(learCredentialEmployee.id())
                        .organizationIdentifier(learCredentialEmployee.credentialSubject().mandate().mandator().organizationIdentifier())
                        .credentialDecoded(decodedCredential)
                        .build()
        );
    }
}
