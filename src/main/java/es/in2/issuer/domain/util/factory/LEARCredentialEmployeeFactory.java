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
    private final DefaultSignerConfig defaultSignerConfig;

    public Mono<String> mapCredentialAndBindMandateeIdInToTheCredential(String learCredential, String mandateeId)
            throws InvalidCredentialFormatException {
        LEARCredentialEmployeeJwtPayload basePayload = mapStringToLEARCredentialEmployeeJwtPayload(learCredential);
        return bindMandateeIdToLearCredentialEmployee(basePayload, mandateeId)
                .flatMap(this::convertLEARCredentialEmployeeInToString);
    }

    public Mono<CredentialProcedureCreationRequest> mapAndBuildLEARCredentialEmployee(JsonNode learCredential, String operationMode) {
        LEARCredentialEmployee.CredentialSubject baseCredentialSubject = mapJsonNodeToCredentialSubject(learCredential);
        return buildFinalLearCredentialEmployee(baseCredentialSubject)
                .flatMap(this::buildLEARCredentialEmployeeJwtPayload)
                .flatMap(payload ->
                        convertLEARCredentialEmployeeInToString(payload)
                                .flatMap(decoded ->
                                        buildCredentialProcedureCreationRequest(decoded, payload, operationMode)
                                )
                );
    }

    public LEARCredentialEmployeeJwtPayload mapStringToLEARCredentialEmployeeJwtPayload(String learCredential)
            throws InvalidCredentialFormatException {
        try {
            log.info(objectMapper.readValue(learCredential, LEARCredentialEmployeeJwtPayload.class).toString());
            return objectMapper.readValue(learCredential, LEARCredentialEmployeeJwtPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing LEARCredentialEmployeeJwtPayload", e);
            throw new InvalidCredentialFormatException("Error parsing LEARCredentialEmployeeJwtPayload");
        }
    }

    public LEARCredentialEmployee mapStringToLEARCredentialEmployee(String learCredential)
            throws InvalidCredentialFormatException {
        try {
            log.info(objectMapper.readValue(learCredential, LEARCredentialEmployee.class).toString());
            return objectMapper.readValue(learCredential, LEARCredentialEmployee.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing LEARCredentialEmployee", e);
            throw new InvalidCredentialFormatException("Error parsing LEARCredentialEmployee");
        }
    }

    private LEARCredentialEmployee.CredentialSubject mapJsonNodeToCredentialSubject(JsonNode jsonNode) {
        LEARCredentialEmployee.CredentialSubject.Mandate mandate =
                objectMapper.convertValue(jsonNode, LEARCredentialEmployee.CredentialSubject.Mandate.class);
        return LEARCredentialEmployee.CredentialSubject.builder()
                .mandate(mandate)
                .build();
    }

    private Mono<LEARCredentialEmployee> buildFinalLearCredentialEmployee(LEARCredentialEmployee.CredentialSubject baseCredentialSubject) {
        Instant currentTime = Instant.now();
        String validFrom = currentTime.toString();
        String validUntil = currentTime.plus(365, ChronoUnit.DAYS).toString();

        List<LEARCredentialEmployee.CredentialSubject.Mandate.Power> populatedPowers = createPopulatedPowers(baseCredentialSubject);
        DetailedIssuer issuer = createIssuer();
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee = createMandatee(baseCredentialSubject);
        LEARCredentialEmployee.CredentialSubject.Mandate mandate = createMandate(baseCredentialSubject, mandatee, populatedPowers);
        LEARCredentialEmployee.CredentialSubject credentialSubject = createCredentialSubject(mandate);

        LEARCredentialEmployee credentialEmployee = LEARCredentialEmployee.builder()
                .context(CREDENTIAL_CONTEXT)
                .id(UUID.randomUUID().toString())
                .type(List.of(LEAR_CREDENTIAL_EMPLOYEE, VERIFIABLE_CREDENTIAL))
                .description(LEAR_CREDENTIAL_EMPLOYEE_DESCRIPTION)
                .issuer(issuer)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .credentialSubject(credentialSubject)
                .build();

        return Mono.just(credentialEmployee);
    }

    private List<LEARCredentialEmployee.CredentialSubject.Mandate.Power> createPopulatedPowers(
            LEARCredentialEmployee.CredentialSubject baseCredentialSubject) {
        return baseCredentialSubject.mandate().power().stream()
                .map(power -> LEARCredentialEmployee.CredentialSubject.Mandate.Power.builder()
                        .id(UUID.randomUUID().toString())
                        .type(power.type())
                        .domain(power.domain())
                        .function(power.function())
                        .action(power.action())
                        .build())
                .toList();
    }

    private DetailedIssuer createIssuer() {
        String issuerId;
        String issuerIdentifier;
        if((remoteSignatureConfig.getRemoteSignatureType()).equals("server")){
            issuerId = DID_ELSI + defaultSignerConfig.getOrganizationIdentifier();
            issuerIdentifier = defaultSignerConfig.getOrganizationIdentifier();
        } else {
            issuerId = DID_ELSI + "VATES-D70795026";
            issuerIdentifier = "VATES-D70795026";
        }
        return DetailedIssuer.builder()
                .id(issuerId)
                .organizationIdentifier(issuerIdentifier)
                .organization(defaultSignerConfig.getOrganization())
                .country(defaultSignerConfig.getCountry())
                .commonName(defaultSignerConfig.getCommonName())
                .emailAddress(defaultSignerConfig.getEmail())
                .serialNumber(defaultSignerConfig.getSerialNumber())
                .build();
    }

    private LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee createMandatee(
            LEARCredentialEmployee.CredentialSubject baseCredentialSubject) {
        return LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                .firstName(baseCredentialSubject.mandate().mandatee().firstName())
                .lastName(baseCredentialSubject.mandate().mandatee().lastName())
                .email(baseCredentialSubject.mandate().mandatee().email())
                .nationality(baseCredentialSubject.mandate().mandatee().nationality())
                .build();
    }

    private LEARCredentialEmployee.CredentialSubject.Mandate createMandate(
            LEARCredentialEmployee.CredentialSubject baseCredentialSubject,
            LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee,
            List<LEARCredentialEmployee.CredentialSubject.Mandate.Power> populatedPowers) {
        return LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                .id(UUID.randomUUID().toString())
                .mandator(baseCredentialSubject.mandate().mandator())
                .mandatee(mandatee)
                .power(populatedPowers)
                .build();
    }

    private LEARCredentialEmployee.CredentialSubject createCredentialSubject(
            LEARCredentialEmployee.CredentialSubject.Mandate mandate) {
        return LEARCredentialEmployee.CredentialSubject.builder()
                .mandate(mandate)
                .build();
    }

    private Mono<LEARCredentialEmployeeJwtPayload> buildLEARCredentialEmployeeJwtPayload(LEARCredentialEmployee learCredentialEmployee) {
        //TODO: Ahora el iss está harcodeado segun el tipo de firma, debe ser dinamico
        String issuer;
        if((remoteSignatureConfig.getRemoteSignatureType()).equals("server")){
            issuer = DID_ELSI + defaultSignerConfig.getOrganizationIdentifier();
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

    private Mono<LEARCredentialEmployeeJwtPayload> bindMandateeIdToLearCredentialEmployee(LEARCredentialEmployeeJwtPayload basePayload, String mandateeId) {
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee baseMandatee =
                basePayload.learCredentialEmployee().credentialSubject().mandate().mandatee();
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee updatedMandatee =
                LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                        .id(mandateeId)
                        .email(baseMandatee.email())
                        .firstName(baseMandatee.firstName())
                        .lastName(baseMandatee.lastName())
                        .nationality(baseMandatee.nationality())
                        .build();

        LEARCredentialEmployee updatedCredentialEmployee = LEARCredentialEmployee.builder()
                .context(basePayload.learCredentialEmployee().context())
                .id(basePayload.learCredentialEmployee().id())
                .type(basePayload.learCredentialEmployee().type())
                .description(basePayload.learCredentialEmployee().description())
                .issuer(basePayload.learCredentialEmployee().getIssuer())
                .validFrom(basePayload.learCredentialEmployee().validFrom())
                .validUntil(basePayload.learCredentialEmployee().validUntil())
                .credentialSubject(
                        LEARCredentialEmployee.CredentialSubject.builder()
                                .mandate(
                                        LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                                                .id(basePayload.learCredentialEmployee().credentialSubject().mandate().id())
                                                .mandator(basePayload.learCredentialEmployee().credentialSubject().mandate().mandator())
                                                .mandatee(updatedMandatee)
                                                .power(basePayload.learCredentialEmployee().credentialSubject().mandate().power())
                                                .build()
                                )
                                .build()
                )
                .build();

        return Mono.just(
                LEARCredentialEmployeeJwtPayload.builder()
                        .learCredentialEmployee(updatedCredentialEmployee)
                        .subject(mandateeId)
                        .JwtId(basePayload.JwtId())
                        .expirationTime(basePayload.expirationTime())
                        .issuedAt(basePayload.issuedAt())
                        .issuer(basePayload.issuer())
                        .notValidBefore(basePayload.notValidBefore())
                        .build()
        );
    }

    private Mono<String> convertLEARCredentialEmployeeInToString(LEARCredentialEmployeeJwtPayload payload) {
        try {
            return Mono.just(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException());
        }
    }

    private Mono<CredentialProcedureCreationRequest> buildCredentialProcedureCreationRequest(String decodedCredential, LEARCredentialEmployeeJwtPayload payload, String operationMode) {
        return accessTokenService.getOrganizationIdFromCurrentSession()
                .flatMap(organizationId ->
                        Mono.just(
                                CredentialProcedureCreationRequest.builder()
                                        .credentialId(payload.learCredentialEmployee().id())
                                        .organizationIdentifier(organizationId)
                                        .credentialDecoded(decodedCredential)
                                        .credentialType(CredentialType.LEAR_CREDENTIAL_EMPLOYEE)
                                        .subject(payload.learCredentialEmployee().credentialSubject().mandate().mandatee().firstName() +
                                                " " +
                                                payload.learCredentialEmployee().credentialSubject().mandate().mandatee().lastName())
                                        .validUntil(parseEpochSecondIntoTimestamp(payload.expirationTime()))
                                        .operationMode(operationMode)
                                        .build()
                        )
                );
    }

    private Timestamp parseEpochSecondIntoTimestamp(Long unixEpochSeconds) {
        return Timestamp.from(Instant.ofEpochSecond(unixEpochSeconds));
    }
}