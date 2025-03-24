package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.InvalidCredentialFormatException;
import es.in2.issuer.domain.exception.RemoteSignatureException;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.dto.SignatureRequest;
import es.in2.issuer.domain.model.dto.credential.DetailedIssuer;
import es.in2.issuer.domain.model.dto.credential.lear.Power;
import es.in2.issuer.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployeeJwtPayload;
import es.in2.issuer.domain.model.enums.CredentialType;
import es.in2.issuer.domain.service.AccessTokenService;
import es.in2.issuer.domain.service.impl.RemoteSignatureServiceImpl;
import es.in2.issuer.infrastructure.config.DefaultSignerConfig;
import es.in2.issuer.infrastructure.config.RemoteSignatureConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static es.in2.issuer.domain.util.Constants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LEARCredentialEmployeeFactory {

    private final ObjectMapper objectMapper;
    private final AccessTokenService accessTokenService;
    private final RemoteSignatureConfig remoteSignatureConfig;
    private final DefaultSignerConfig defaultSignerConfig;
    private final RemoteSignatureServiceImpl remoteSignatureServiceImpl;

    public Mono<String> mapCredentialAndBindMandateeIdInToTheCredential(String decodedCredentialString, String mandateeId)
            throws InvalidCredentialFormatException {
        LEARCredentialEmployee decodedCredential = mapStringToLEARCredentialEmployee(decodedCredentialString);
        return bindMandateeIdToLearCredentialEmployee(decodedCredential, mandateeId)
                .flatMap(this::convertLEARCredentialEmployeeInToString);
    }

    public Mono<String> mapCredentialAndBindIssuerInToTheCredential(String decodedCredentialString, String procedureId)
            throws InvalidCredentialFormatException {
        LEARCredentialEmployee decodedCredential = mapStringToLEARCredentialEmployee(decodedCredentialString);
        return bindIssuerToLearCredentialEmployee(decodedCredential, procedureId)
                .flatMap(this::convertLEARCredentialEmployeeInToString);
    }

    public Mono<CredentialProcedureCreationRequest> mapAndBuildLEARCredentialEmployee(JsonNode learCredential, String operationMode) {
        LEARCredentialEmployee.CredentialSubject baseCredentialSubject = mapJsonNodeToCredentialSubject(learCredential);
        return buildFinalLearCredentialEmployee(baseCredentialSubject)
                .flatMap(credentialDecoded ->
                        convertLEARCredentialEmployeeInToString(credentialDecoded)
                                .flatMap(credentialDecodedString ->
                                        buildCredentialProcedureCreationRequest(credentialDecodedString, credentialDecoded, operationMode)
                                )
                );
    }

    public LEARCredentialEmployee mapStringToLEARCredentialEmployee(String learCredential)
            throws InvalidCredentialFormatException {
        try {
            LEARCredentialEmployee employee = objectMapper.readValue(learCredential, LEARCredentialEmployee.class);
            log.info(employee.toString());
            return employee;
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

        List<Power> populatedPowers = createPopulatedPowers(baseCredentialSubject);
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee = createMandatee(baseCredentialSubject);
        LEARCredentialEmployee.CredentialSubject.Mandate mandate = createMandate(baseCredentialSubject, mandatee, populatedPowers);
        LEARCredentialEmployee.CredentialSubject credentialSubject = createCredentialSubject(mandate);

        LEARCredentialEmployee credentialEmployee = LEARCredentialEmployee.builder()
                .context(CREDENTIAL_CONTEXT)
                .id(UUID.randomUUID().toString())
                .type(List.of(LEAR_CREDENTIAL_EMPLOYEE, VERIFIABLE_CREDENTIAL))
                .description(LEAR_CREDENTIAL_EMPLOYEE_DESCRIPTION)
                .credentialSubject(credentialSubject)
                .validFrom(validFrom)
                .validUntil(validUntil)
                .build();

        return Mono.just(credentialEmployee);
    }

    private List<Power> createPopulatedPowers(
            LEARCredentialEmployee.CredentialSubject baseCredentialSubject) {
        return baseCredentialSubject.mandate().power().stream()
                .map(power -> Power.builder()
                        .id(UUID.randomUUID().toString())
                        .type(power.type())
                        .domain(power.domain())
                        .function(power.function())
                        .action(power.action())
                        .build())
                .toList();
    }

    private Mono<DetailedIssuer> createIssuer(String procedureId) {
        if (remoteSignatureConfig.getRemoteSignatureType().equals("server")) {
            return Mono.just(DetailedIssuer.builder()
                    .id(DID_ELSI + defaultSignerConfig.getOrganizationIdentifier())
                    .organizationIdentifier(defaultSignerConfig.getOrganizationIdentifier())
                    .organization(defaultSignerConfig.getOrganization())
                    .country(defaultSignerConfig.getCountry())
                    .commonName(defaultSignerConfig.getCommonName())
                    .emailAddress(defaultSignerConfig.getEmail())
                    .serialNumber(defaultSignerConfig.getSerialNumber())
                    .build());
        } else {
            return createIssuerRemote(procedureId);
        }
    }

    private Mono<DetailedIssuer> createIssuerRemote(String procedureId) {
        return Mono.defer(() -> remoteSignatureServiceImpl.validateCredentials()
                .flatMap(valid -> {
                    if (valid) {
                        return remoteSignatureServiceImpl.requestAccessToken(SignatureRequest.builder().build(), "service")
                                .flatMap(accessToken -> remoteSignatureServiceImpl.requestCertificateInfo(accessToken, remoteSignatureConfig.getRemoteSignatureCredentialId()))
                                .flatMap(certificateInfo -> remoteSignatureServiceImpl.extractIssuerFromCertificateInfo(certificateInfo, procedureId));
                    } else {
                        log.error("Credentials mismatch. Signature process aborted.");
                        return Mono.error(new RemoteSignatureException("Credentials mismatch. Signature process aborted."));
                    }
                }))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(5))
                        .jitter(0.5)
                        //.filter(remoteSignatureServiceImpl::isRecoverableError)
                        .doBeforeRetry(retrySignal -> {
                            long attempt = retrySignal.totalRetries() + 1;
                            log.info("Retrying credential validation due to recoverable error (Attempt #{} of 3)", attempt);
                        }))
                .onErrorResume(throwable -> {
                    boolean isRetryExhausted = !(throwable instanceof RemoteSignatureException &&
                            throwable.getMessage().contains("Credentials mismatch"));

                    if (isRetryExhausted) {
                        log.error("Error after 3 retries, switching to ASYNC mode.");
                    }
                    log.error("Error Time: {}", new Date());
                    return remoteSignatureServiceImpl.handlePostRecoverError(throwable, procedureId)
                            .then(Mono.error(new RemoteSignatureException("Signature Failed, changed to ASYNC mode", throwable)));
                });
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
            List<Power> populatedPowers) {
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

    public Mono<LEARCredentialEmployeeJwtPayload> buildLEARCredentialEmployeeJwtPayload(LEARCredentialEmployee learCredentialEmployee) {
        return Mono.just(
                LEARCredentialEmployeeJwtPayload.builder()
                        .JwtId(UUID.randomUUID().toString())
                        .learCredentialEmployee(learCredentialEmployee)
                        .expirationTime(parseDateToUnixTime(learCredentialEmployee.validUntil()))
                        .issuedAt(parseDateToUnixTime(learCredentialEmployee.validFrom()))
                        .notValidBefore(parseDateToUnixTime(learCredentialEmployee.validFrom()))
                        .issuer(learCredentialEmployee.issuer().getId())
                        .subject(learCredentialEmployee.credentialSubject().mandate().mandatee().id())
                        .build()
        );
    }

    private long parseDateToUnixTime(String date) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        return zonedDateTime.toInstant().getEpochSecond();
    }

    private Mono<LEARCredentialEmployee> bindMandateeIdToLearCredentialEmployee(LEARCredentialEmployee decodedCredential, String mandateeId) {
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee baseMandatee =
                decodedCredential.credentialSubject().mandate().mandatee();
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee updatedMandatee =
                LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                        .id(mandateeId)
                        .email(baseMandatee.email())
                        .firstName(baseMandatee.firstName())
                        .lastName(baseMandatee.lastName())
                        .nationality(baseMandatee.nationality())
                        .build();

        return Mono.just( LEARCredentialEmployee.builder()
                .context(decodedCredential.context())
                .id(decodedCredential.id())
                .type(decodedCredential.type())
                .description(decodedCredential.description())
                .issuer(decodedCredential.issuer())
                .validFrom(decodedCredential.validFrom())
                .validUntil(decodedCredential.validUntil())
                .credentialSubject(
                        LEARCredentialEmployee.CredentialSubject.builder()
                                .mandate(
                                        LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                                                .id(decodedCredential.credentialSubject().mandate().id())
                                                .mandator(decodedCredential.credentialSubject().mandate().mandator())
                                                .mandatee(updatedMandatee)
                                                .power(decodedCredential.credentialSubject().mandate().power())
                                                .build()
                                )
                                .build()
                )
                .build()
        );
    }

    private Mono<LEARCredentialEmployee> bindIssuerToLearCredentialEmployee(LEARCredentialEmployee decodedCredential, String procedureId) {
        return createIssuer(procedureId)
                .map(issuer -> LEARCredentialEmployee.builder()
                    .context(decodedCredential.context())
                    .id(decodedCredential.id())
                    .type(decodedCredential.type())
                    .description(decodedCredential.description())
                    .issuer(issuer)
                    .validFrom(decodedCredential.validFrom())
                    .validUntil(decodedCredential.validUntil())
                    .credentialSubject(decodedCredential.credentialSubject())
                    .build());
    }

    private Mono<String> convertLEARCredentialEmployeeInToString(LEARCredentialEmployee credentialDecoded) {
        try {
            return Mono.just(objectMapper.writeValueAsString(credentialDecoded));
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException());
        }
    }

    public Mono<String> convertLEARCredentialEmployeeJwtPayloadInToString(LEARCredentialEmployeeJwtPayload credential) {
        try {
            return Mono.just(objectMapper.writeValueAsString(credential));
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException());
        }
    }

    private Mono<CredentialProcedureCreationRequest> buildCredentialProcedureCreationRequest(String decodedCredential, LEARCredentialEmployee credentialDecoded, String operationMode) {
        return accessTokenService.getOrganizationIdFromCurrentSession()
                .flatMap(organizationId ->
                        Mono.just(
                                CredentialProcedureCreationRequest.builder()
                                        .credentialId(credentialDecoded.id())
                                        .organizationIdentifier(organizationId)
                                        .credentialDecoded(decodedCredential)
                                        .credentialType(CredentialType.LEAR_CREDENTIAL_EMPLOYEE)
                                        .subject(credentialDecoded.credentialSubject().mandate().mandatee().firstName() +
                                                " " +
                                                credentialDecoded.credentialSubject().mandate().mandatee().lastName())
                                        .validUntil(parseEpochSecondIntoTimestamp(parseDateToUnixTime(credentialDecoded.validUntil())))
                                        .operationMode(operationMode)
                                        .build()
                        )
                );
    }

    private Timestamp parseEpochSecondIntoTimestamp(Long unixEpochSeconds) {
        return Timestamp.from(Instant.ofEpochSecond(unixEpochSeconds));
    }
}