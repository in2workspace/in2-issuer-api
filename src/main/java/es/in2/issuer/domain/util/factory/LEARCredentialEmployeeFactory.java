package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.InvalidCredentialFormatException;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployee;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployeeJwtPayload;
import es.in2.issuer.domain.model.enums.CredentialType;
import es.in2.issuer.domain.service.AccessTokenService;
import es.in2.issuer.infrastructure.config.RemoteSignatureConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LEARCredentialEmployeeFactory {

    private final ObjectMapper objectMapper;
    private final AccessTokenService accessTokenService;
    private final RemoteSignatureConfig remoteSignatureConfig;

    public Mono<String> mapCredentialAndBindMandateeIdInToTheCredential(String learCredential, String mandateeId) throws InvalidCredentialFormatException {
        LEARCredentialEmployeeJwtPayload baseLearCredentialEmployee = mapStringToLEARCredentialEmployeeJwtPayload(learCredential);
        return bindMandateeIdToLearCredentialEmployee(baseLearCredentialEmployee, mandateeId)
                .flatMap(this::convertLEARCredentialEmployeeInToString);
    }

    public Mono<CredentialProcedureCreationRequest> mapAndBuildLEARCredentialEmployee(JsonNode learCredential, String operationMode) {
        LEARCredentialEmployee.CredentialSubject baseLearCredentialEmployee = mapJsonNodeToCredentialSubject(learCredential);

        return buildFinalLearCredentialEmployee(baseLearCredentialEmployee)
                .flatMap(this::buildLEARCredentialEmployeeJwtPayload)
                .flatMap(learCredentialEmployeeJwtPayload -> convertLEARCredentialEmployeeInToString(learCredentialEmployeeJwtPayload)
                        .flatMap(decodedCredential -> buildCredentialProcedureCreationRequest(decodedCredential, learCredentialEmployeeJwtPayload, operationMode))
                );
    }

    public LEARCredentialEmployeeJwtPayload mapStringToLEARCredentialEmployeeJwtPayload(String learCredential) throws InvalidCredentialFormatException {
        try {
            log.info(objectMapper.readValue(learCredential, LEARCredentialEmployeeJwtPayload.class).toString());
            return objectMapper.readValue(learCredential, LEARCredentialEmployeeJwtPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing LEARCredentialEmployeeJwtPayload", e);
            throw new InvalidCredentialFormatException("Error parsing LEARCredentialEmployeeJwtPayload");
        }
    }

    public LEARCredentialEmployee mapStringToLEARCredentialEmployee(String learCredential) throws InvalidCredentialFormatException {
        try {
            log.info(objectMapper.readValue(learCredential, LEARCredentialEmployee.class).toString());
            return objectMapper.readValue(learCredential, LEARCredentialEmployee.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing LEARCredentialEmployee", e);
            throw new InvalidCredentialFormatException("Error parsing LEARCredentialEmployee");
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
        String issuanceDate = currentTime.toString();
        String expirationDate = currentTime.plus(365, ChronoUnit.DAYS).toString();

        // Creando una lista nueva de powers con nuevos IDs
        List<LEARCredentialEmployee.CredentialSubject.Mandate.Power> populatedPowers = baseLearCredentialEmployee.mandate().power().stream()
                .map(power -> LEARCredentialEmployee.CredentialSubject.Mandate.Power.builder()
                        .id(UUID.randomUUID().toString())
                        .tmfType(power.tmfType())
                        .tmfDomain(power.tmfDomain())
                        .tmfFunction(power.tmfFunction())
                        .tmfAction(power.tmfAction())
                        .build())
                .toList();
        //TODO: Ahora el issuer está harcodeado segun el tipo de firma, debe ser dinamico
        String issuer;
        if((remoteSignatureConfig.getRemoteSignatureType()).equals("server")){
            issuer = DID_ELSI + baseLearCredentialEmployee.mandate().signer().organizationIdentifier();
        } else {
            issuer = DID_ELSI + "VATES-D70795026";
        }
        return Mono.just(LEARCredentialEmployee.builder()
                .context(CREDENTIAL_CONTEXT)
                .id(UUID.randomUUID().toString())
                .type(List.of(LEAR_CREDENTIAL_EMPLOYEE, VERIFIABLE_CREDENTIAL))
                .issuer(issuer)
                .validFrom(issuanceDate)
                .validUntil(expirationDate)
                .credentialSubject(LEARCredentialEmployee.CredentialSubject.builder()
                        .mandate(LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                                .id(UUID.randomUUID().toString())
                                .mandator(baseLearCredentialEmployee.mandate().mandator())
                                .mandatee(baseLearCredentialEmployee.mandate().mandatee())
                                .power(populatedPowers)
                                .signer(baseLearCredentialEmployee.mandate().signer())
                                .lifeSpan(LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan.builder()
                                        .startDateTime(issuanceDate)
                                        .endDateTime(expirationDate)
                                        .build())
                                .build())
                        .build())
                .build());
    }

    private Mono<LEARCredentialEmployeeJwtPayload> buildLEARCredentialEmployeeJwtPayload(LEARCredentialEmployee learCredentialEmployee) {
        //TODO: Ahora el iss está harcodeado segun el tipo de firma, debe ser dinamico
        String issuer;
        if((remoteSignatureConfig.getRemoteSignatureType()).equals("server")){
            issuer = DID_ELSI + learCredentialEmployee.credentialSubject().mandate().signer().organizationIdentifier();
        } else {
            issuer = DID_ELSI + "VATES-D70795026";
        }
        return Mono.just(
                LEARCredentialEmployeeJwtPayload.builder()
                        .JwtId(UUID.randomUUID().toString())
                        .learCredentialEmployee(learCredentialEmployee)
                        .expirationTime(parseDateToUnixTime(learCredentialEmployee.validUntil()))
                        .issuedAt(parseDateToUnixTime(learCredentialEmployee.validFrom()))
                        .notValidBefore(parseDateToUnixTime(learCredentialEmployee.validFrom()))
                        .issuer(issuer)
                        .subject(learCredentialEmployee.credentialSubject().mandate().mandatee().id())
                        .build()
        );
    }

    private long parseDateToUnixTime(String date) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        return zonedDateTime.toInstant().getEpochSecond();
    }

    private Mono<LEARCredentialEmployeeJwtPayload> bindMandateeIdToLearCredentialEmployee(LEARCredentialEmployeeJwtPayload baseLearCredentialEmployee, String mandateeId) {
        return Mono.just(
                LEARCredentialEmployeeJwtPayload.builder().learCredentialEmployee(
                                LEARCredentialEmployee.builder()
                                        .context(baseLearCredentialEmployee.learCredentialEmployee().context())
                                        .id(baseLearCredentialEmployee.learCredentialEmployee().id())
                                        .type(baseLearCredentialEmployee.learCredentialEmployee().type())
                                        .issuer(baseLearCredentialEmployee.issuer())
                                        .validFrom(baseLearCredentialEmployee.learCredentialEmployee().validFrom())
                                        .validUntil(baseLearCredentialEmployee.learCredentialEmployee().validUntil())
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

    private Mono<String> convertLEARCredentialEmployeeInToString(LEARCredentialEmployeeJwtPayload learCredentialEmployeeJwtPayload) {
        try {

            return Mono.just(objectMapper.writeValueAsString(learCredentialEmployeeJwtPayload));
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException());
        }
    }

    private Mono<CredentialProcedureCreationRequest> buildCredentialProcedureCreationRequest(String decodedCredential, LEARCredentialEmployeeJwtPayload learCredentialEmployeeJwtPayload, String operationMode) {
        return accessTokenService.getOrganizationIdFromCurrentSession()
                .flatMap(organizationId ->
                        Mono.just(
                                CredentialProcedureCreationRequest.builder()
                                        .credentialId(learCredentialEmployeeJwtPayload.learCredentialEmployee().id())
                                        .organizationIdentifier(organizationId)
                                        .credentialDecoded(decodedCredential)
                                        .credentialType(CredentialType.LEAR_CREDENTIAL_EMPLOYEE)
                                        .subject(learCredentialEmployeeJwtPayload.learCredentialEmployee().credentialSubject().mandate().mandatee().firstName() +
                                                " " +
                                                learCredentialEmployeeJwtPayload.learCredentialEmployee().credentialSubject().mandate().mandatee().lastName())
                                        .validUntil(parseEpochSecondIntoTimestamp(learCredentialEmployeeJwtPayload.expirationTime()))
                                        .operationMode(operationMode)
                                        .build()
                        )
                );
    }
    private Timestamp parseEpochSecondIntoTimestamp(Long unixEpochSeconds) {
        return Timestamp.from(Instant.ofEpochSecond(unixEpochSeconds));
    }

}
