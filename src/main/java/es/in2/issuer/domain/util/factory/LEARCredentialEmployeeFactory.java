package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.InvalidCredentialFormatException;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.dto.DetailedIssuer;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployee;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployeeJwtPayload;
import es.in2.issuer.domain.model.enums.CredentialType;
import es.in2.issuer.domain.service.AccessTokenService;
import es.in2.issuer.infrastructure.config.DefaultSignerConfig;
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
    private final DefaultSignerConfig defaultSignerConfig;

    public Mono<String> mapCredentialAndBindMandateeIdInToTheCredential(String learCredential, String mandateeId) throws InvalidCredentialFormatException {
        LEARCredentialEmployeeJwtPayload baseLearCredentialEmployee = mapStringToLEARCredentialEmployeeJwtPayload(learCredential);
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
        String validFrom = currentTime.toString();
        String validUntil = currentTime.plus(365, ChronoUnit.DAYS).toString();

        // Creando una lista nueva de powers con nuevos IDs
        List<LEARCredentialEmployee.CredentialSubject.Mandate.Power> populatedPowers = baseLearCredentialEmployee.mandate().power().stream()
                .map(power -> LEARCredentialEmployee.CredentialSubject.Mandate.Power.builder()
                        .id(UUID.randomUUID().toString())
                        .type(power.type())
                        .domain(power.domain())
                        .function(power.function())
                        .action(power.action())
                        .build())
                .toList();

        return Mono.just(LEARCredentialEmployee.builder()
                .context(CREDENTIAL_CONTEXT)
                .id(UUID.randomUUID().toString())
                .type(List.of(LEAR_CREDENTIAL_EMPLOYEE, VERIFIABLE_CREDENTIAL))
                .description(LEAR_CREDENTIAL_EMPLOYEE_DESCRIPTION)
                .issuer(DetailedIssuer.builder()
                        .id(DID_ELSI + defaultSignerConfig.getOrganizationIdentifier())
                        .organizationIdentifier(defaultSignerConfig.getOrganizationIdentifier())
                        .organization(defaultSignerConfig.getOrganization())
                        .country(defaultSignerConfig.getCountry())
                        .commonName(defaultSignerConfig.getCommonName())
                        .emailAddress(defaultSignerConfig.getEmail())
                        .serialNumber(defaultSignerConfig.getSerialNumber())
                        .build())
                .validFrom(validFrom)
                .validUntil(validUntil)
                .credentialSubject(LEARCredentialEmployee.CredentialSubject.builder()
                        .mandate(LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                                .id(UUID.randomUUID().toString())
                                .mandator(baseLearCredentialEmployee.mandate().mandator())
                                .mandatee(LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                                        .firstName(baseLearCredentialEmployee.mandate().mandatee().firstName())
                                        .lastName(baseLearCredentialEmployee.mandate().mandatee().lastName())
                                        .email(baseLearCredentialEmployee.mandate().mandatee().email())
                                        .nationality(baseLearCredentialEmployee.mandate().mandatee().nationality())
                                        .build())
                                .power(populatedPowers)
                                .build())
                        .build())
                .build());
    }

    private Mono<LEARCredentialEmployeeJwtPayload> buildLEARCredentialEmployeeJwtPayload(LEARCredentialEmployee learCredentialEmployee) {
        return Mono.just(
                LEARCredentialEmployeeJwtPayload.builder()
                        .JwtId(UUID.randomUUID().toString())
                        .learCredentialEmployee(learCredentialEmployee)
                        .expirationTime(parseDateToUnixTime(learCredentialEmployee.validUntil()))
                        .issuedAt(parseDateToUnixTime(learCredentialEmployee.validFrom()))
                        .notValidBefore(parseDateToUnixTime(learCredentialEmployee.validFrom()))
                        .issuer(DID_ELSI + defaultSignerConfig.getOrganizationIdentifier())
                        .subject(learCredentialEmployee.credentialSubject().mandate().mandatee().id())
                        .build()
        );
    }

    private long parseDateToUnixTime(String date) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        return zonedDateTime.toInstant().getEpochSecond();
    }

    private Mono<LEARCredentialEmployeeJwtPayload> bindMandateeIdToLearCredentialEmployee(LEARCredentialEmployeeJwtPayload baseLearCredentialEmployee, String mandateeId) {
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee = baseLearCredentialEmployee.learCredentialEmployee().credentialSubject().mandate().mandatee();
        return Mono.just(
                LEARCredentialEmployeeJwtPayload.builder().learCredentialEmployee(
                                LEARCredentialEmployee.builder()
                                        .context(baseLearCredentialEmployee.learCredentialEmployee().context())
                                        .id(baseLearCredentialEmployee.learCredentialEmployee().id())
                                        .type(baseLearCredentialEmployee.learCredentialEmployee().type())
                                        .issuer(baseLearCredentialEmployee.learCredentialEmployee().getIssuer())
                                        .validFrom(baseLearCredentialEmployee.learCredentialEmployee().validFrom())
                                        .validUntil(baseLearCredentialEmployee.learCredentialEmployee().validUntil())
                                        .credentialSubject(LEARCredentialEmployee.CredentialSubject.builder()
                                                .mandate(LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                                                        .id(baseLearCredentialEmployee.learCredentialEmployee().credentialSubject().mandate().id())
                                                        .mandator(baseLearCredentialEmployee.learCredentialEmployee().credentialSubject().mandate().mandator())
                                                        .mandatee(LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                                                                .id(mandateeId)
                                                                .email(mandatee.email())
                                                                .firstName(mandatee.firstName())
                                                                .lastName(mandatee.lastName())
                                                                .nationality(mandatee.nationality())
                                                                .build())
                                                        .power(baseLearCredentialEmployee.learCredentialEmployee().credentialSubject().mandate().power())
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

    private Mono<CredentialProcedureCreationRequest> buildCredentialProcedureCreationRequest(String decodedCredential, LEARCredentialEmployeeJwtPayload learCredentialEmployeeJwtPayload) {
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
                                        .build()
                        )
                );
    }
    private Timestamp parseEpochSecondIntoTimestamp(Long unixEpochSeconds) {
        return Timestamp.from(Instant.ofEpochSecond(unixEpochSeconds));
    }

}
