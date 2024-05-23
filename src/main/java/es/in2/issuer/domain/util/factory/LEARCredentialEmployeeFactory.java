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

    public Mono<String> mapCredentialAndBindMandateeIdInToTheCredential(String learCredential, String mandateeId){
        LEARCredentialEmployee baseLearCredentialEmployee = mapStringToLEARCredentialEmployee(learCredential);
        return bindMandateeIdToLearCredentialEmployee(baseLearCredentialEmployee, mandateeId)
                .flatMap(this::convertLEARCredentialEmployeeInToString);
    }
    public Mono<CredentialProcedureCreationRequest> mapAndBuildLEARCredentialEmployee(JsonNode learCredential){
        LEARCredentialEmployee.CredentialSubject baseLearCredentialEmployee = mapJsonNodeToCredentialSubject(learCredential);

        return buildFinalLearCredentialEmployee(baseLearCredentialEmployee)
                .flatMap(learCredentialEmployee -> convertLEARCredentialEmployeeInToString(learCredentialEmployee)
                    .flatMap(decodedCredential -> buildCredentialProcedureCreationRequest(decodedCredential,learCredentialEmployee))
                );
    }
    private LEARCredentialEmployee mapStringToLEARCredentialEmployee(String learCredential){
        try {
            return objectMapper.readValue(learCredential, LEARCredentialEmployee.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private LEARCredentialEmployee.CredentialSubject mapJsonNodeToCredentialSubject(JsonNode jsonNode) {

        LEARCredentialEmployee.CredentialSubject.Mandate mandate = objectMapper.convertValue(jsonNode, LEARCredentialEmployee.CredentialSubject.Mandate.class);
        return LEARCredentialEmployee.CredentialSubject.builder()
                .mandate(mandate)
                .build();
    }

    private Mono<LEARCredentialEmployee> buildFinalLearCredentialEmployee(LEARCredentialEmployee.CredentialSubject baseLearCredentialEmployee){
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
                                .id(UUID.randomUUID().toString())
                                .mandator(baseLearCredentialEmployee.mandate().mandator())
                                .mandatee(baseLearCredentialEmployee.mandate().mandatee())
                                .power(baseLearCredentialEmployee.mandate().power())
                                .lifeSpan(LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan.builder()
                                        .startDateTime(currentTime.toString())
                                        .endDateTime(currentTime.plus(30, ChronoUnit.DAYS).toString())
                                        .build())
                                .build())
                        .build())
                .build());
    }

    private Mono<LEARCredentialEmployee> bindMandateeIdToLearCredentialEmployee(LEARCredentialEmployee baseLearCredentialEmployee, String mandateeId){

        return Mono.just(LEARCredentialEmployee.builder()
                .expirationDate(baseLearCredentialEmployee.expirationDate())
                .issuanceDate(baseLearCredentialEmployee.issuanceDate())
                .validFrom(baseLearCredentialEmployee.validFrom())
                .id(baseLearCredentialEmployee.id())
                .type(baseLearCredentialEmployee.type())
                .issuer(baseLearCredentialEmployee.issuer())
                .credentialSubject(LEARCredentialEmployee.CredentialSubject.builder()
                        .mandate(LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                                .mandator(baseLearCredentialEmployee.credentialSubject().mandate().mandator())
                                .mandatee(LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                                        .id(mandateeId)
                                        .email(baseLearCredentialEmployee.credentialSubject().mandate().mandatee().email())
                                        .gender(baseLearCredentialEmployee.credentialSubject().mandate().mandatee().gender())
                                        .firstName(baseLearCredentialEmployee.credentialSubject().mandate().mandatee().firstName())
                                        .lastName(baseLearCredentialEmployee.credentialSubject().mandate().mandatee().lastName())
                                        .mobilePhone(baseLearCredentialEmployee.credentialSubject().mandate().mandatee().mobilePhone())
                                        .build())
                                .power(baseLearCredentialEmployee.credentialSubject().mandate().power())
                                .lifeSpan(baseLearCredentialEmployee.credentialSubject().mandate().lifeSpan())
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
