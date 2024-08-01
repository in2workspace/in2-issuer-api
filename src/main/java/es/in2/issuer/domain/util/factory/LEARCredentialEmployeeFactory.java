package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployee;
import es.in2.issuer.domain.model.dto.learCredentialEmployeeJwtPayload;
import es.in2.issuer.domain.service.AccessTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class LEARCredentialEmployeeFactory {

    private final ObjectMapper objectMapper;
    private final AccessTokenService accessTokenService;

    public Mono<String> mapCredentialAndBindMandateeIdInToTheCredential(String learCredential, String mandateeId) {
        learCredentialEmployeeJwtPayload baseLearCredentialEmployee = mapStringToLEARCredentialEmployee(learCredential);
        return bindMandateeIdToLearCredentialEmployee(baseLearCredentialEmployee, mandateeId)
                .flatMap(this::convertLEARCredentialEmployeeInToString);
    }

    public Mono<CredentialProcedureCreationRequest> mapAndBuildLEARCredentialEmployee(JsonNode learCredential) {
        LEARCredentialEmployee.CredentialSubject baseLearCredentialEmployee = mapJsonNodeToCredentialSubject(learCredential);

        return buildFinalLearCredentialEmployee(baseLearCredentialEmployee)
                .flatMap(this::buildLEARCredentialEmployeeJwtPayload)
                .flatMap(learCredentialEmployeeJwtPayload -> convertLEARCredentialEmployeeInToString(learCredentialEmployeeJwtPayload)
                        .flatMap(decodedCredential -> buildCredentialProcedureCreationRequest(decodedCredential, learCredentialEmployeeJwtPayload))
                );
    }

    private learCredentialEmployeeJwtPayload mapStringToLEARCredentialEmployee(String learCredential) {
        try {
            log.info(objectMapper.readValue(learCredential, learCredentialEmployeeJwtPayload.class).toString());
            return objectMapper.readValue(learCredential, learCredentialEmployeeJwtPayload.class);
        } catch (JsonProcessingException e) {
            // fixme: handle exception and return a custom exception
            throw new RuntimeException(e);
        }
    }

    private LEARCredentialEmployee.CredentialSubject mapJsonNodeToCredentialSubject(JsonNode jsonNode) {

        LEARCredentialEmployee.CredentialSubject.Mandate mandate = objectMapper.convertValue(jsonNode, LEARCredentialEmployee.CredentialSubject.Mandate.class);
        return LEARCredentialEmployee.CredentialSubject.builder()
                .mandate(mandate)
                .build();
    }

    private Mono<LEARCredentialEmployee> buildFinalLearCredentialEmployee(LEARCredentialEmployee.CredentialSubject baseLearCredentialEmployee) {
        Instant currentTime = Instant.now();

        // Creando una lista nueva de powers con nuevos IDs
        List<LEARCredentialEmployee.CredentialSubject.Mandate.Power> populatedPowers = baseLearCredentialEmployee.mandate().power().stream()
                .map(power -> LEARCredentialEmployee.CredentialSubject.Mandate.Power.builder()
                        .id(UUID.randomUUID().toString())
                        .tmfType(power.tmfType())
                        .tmfAction(power.tmfAction())
                        .tmfFunction(power.tmfFunction())
                        .build())
                .toList();


        return Mono.just(LEARCredentialEmployee.builder()
                .expirationDate(currentTime.plus(30, ChronoUnit.DAYS).toString())
                .issuanceDate(currentTime.toString())
                .validFrom(currentTime.toString())
                .id(UUID.randomUUID().toString())
                .context(CREDENTIAL_CONTEXT)
                .type(List.of(LEAR_CREDENTIAL_EMPLOYEE, VERIFIABLE_CREDENTIAL))
                .issuer(DID_ELSI + baseLearCredentialEmployee.mandate().mandator().organizationIdentifier())
                .credentialSubject(LEARCredentialEmployee.CredentialSubject.builder()
                        .mandate(LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                                .id(UUID.randomUUID().toString())
                                .mandator(baseLearCredentialEmployee.mandate().mandator())
                                .mandatee(baseLearCredentialEmployee.mandate().mandatee())
                                .power(populatedPowers)
                                .signer(baseLearCredentialEmployee.mandate().signer())
                                .lifeSpan(LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan.builder()
                                        .startDateTime(currentTime.toString())
                                        .endDateTime(currentTime.plus(30, ChronoUnit.DAYS).toString())
                                        .build())
                                .build())
                        .build())
                .build());
    }


    private Mono<learCredentialEmployeeJwtPayload> buildLEARCredentialEmployeeJwtPayload(LEARCredentialEmployee learCredentialEmployee){
        return Mono.just(
                learCredentialEmployeeJwtPayload.builder()
                        .JwtId(UUID.randomUUID().toString())
                        .learCredentialEmployee(learCredentialEmployee)
                        .expirationTime(parseDateToUnixTime(learCredentialEmployee.expirationDate()))
                        .issuedAt(parseDateToUnixTime(learCredentialEmployee.issuanceDate()))
                        .notValidBefore(parseDateToUnixTime(learCredentialEmployee.validFrom()))
                        .issuer(learCredentialEmployee.issuer())
                        .subject(learCredentialEmployee.credentialSubject().mandate().mandatee().id())
                        .build()
        );
    }

    private long parseDateToUnixTime(String date) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        return zonedDateTime.toInstant().getEpochSecond();
    }

    private Mono<learCredentialEmployeeJwtPayload> bindMandateeIdToLearCredentialEmployee(learCredentialEmployeeJwtPayload baseLearCredentialEmployee, String mandateeId) {
        return Mono.just(
                learCredentialEmployeeJwtPayload.builder().learCredentialEmployee(
                        LEARCredentialEmployee.builder()
                                .expirationDate(baseLearCredentialEmployee.learCredentialEmployee().expirationDate())
                                .issuanceDate(baseLearCredentialEmployee.learCredentialEmployee().issuanceDate())
                                .validFrom(baseLearCredentialEmployee.learCredentialEmployee().validFrom())
                                .id(baseLearCredentialEmployee.learCredentialEmployee().id())
                                .context(baseLearCredentialEmployee.learCredentialEmployee().context())
                                .type(baseLearCredentialEmployee.learCredentialEmployee().type())
                                .issuer(baseLearCredentialEmployee.issuer())
                                .credentialSubject(LEARCredentialEmployee.CredentialSubject.builder()
                                        .mandate(LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                                                .id(baseLearCredentialEmployee.learCredentialEmployee().credentialSubject().mandate().id())
                                                .mandator(baseLearCredentialEmployee.learCredentialEmployee().credentialSubject().mandate().mandator())
                                                .mandatee(LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                                                        .id(mandateeId)
                                                        .email(baseLearCredentialEmployee.learCredentialEmployee().credentialSubject().mandate().mandatee().email())
                                                        .firstName(baseLearCredentialEmployee.learCredentialEmployee().credentialSubject().mandate().mandatee().firstName())
                                                        .lastName(baseLearCredentialEmployee.learCredentialEmployee().credentialSubject().mandate().mandatee().lastName())
                                                        .mobilePhone(baseLearCredentialEmployee.learCredentialEmployee().credentialSubject().mandate().mandatee().mobilePhone())
                                                        .build())
                                                .power(baseLearCredentialEmployee.learCredentialEmployee().credentialSubject().mandate().power())
                                                .signer(baseLearCredentialEmployee.learCredentialEmployee().credentialSubject().mandate().signer())
                                                .lifeSpan(baseLearCredentialEmployee.learCredentialEmployee().credentialSubject().mandate().lifeSpan())
                                                .build())
                                        .build())
                                .build())
                        .subject(mandateeId)
                        .JwtId(baseLearCredentialEmployee.JwtId())
                        .expirationTime(baseLearCredentialEmployee.expirationTime())
                        .issuedAt(baseLearCredentialEmployee.issuedAt())
                        .issuer(baseLearCredentialEmployee.issuer())
                        .notValidBefore(baseLearCredentialEmployee.notValidBefore())
                        .build());
    }

    private Mono<String> convertLEARCredentialEmployeeInToString(learCredentialEmployeeJwtPayload LEARCredentialEmployeeJwtPayload) {
        try {

            return Mono.just(objectMapper.writeValueAsString(LEARCredentialEmployeeJwtPayload));
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException());
        }
    }

    private Mono<CredentialProcedureCreationRequest> buildCredentialProcedureCreationRequest(String decodedCredential, learCredentialEmployeeJwtPayload LEARCredentialEmployeeJwtPayload) {
        return accessTokenService.getOrganizationIdFromCurrentSession()
                .flatMap(organizationId ->
                        Mono.just(
                                CredentialProcedureCreationRequest.builder()
                                        .credentialId(LEARCredentialEmployeeJwtPayload.learCredentialEmployee().id())
                                        .organizationIdentifier(organizationId)
                                        .credentialDecoded(decodedCredential)
                                        .build()
                        )
                );
    }

}
