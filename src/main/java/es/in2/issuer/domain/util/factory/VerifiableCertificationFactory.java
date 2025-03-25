package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.InvalidCredentialFormatException;
import es.in2.issuer.domain.exception.ParseErrorException;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.domain.model.dto.VerifiableCertification;
import es.in2.issuer.domain.model.dto.VerifiableCertificationJwtPayload;
import es.in2.issuer.domain.model.enums.CredentialType;
import es.in2.issuer.domain.service.JWTService;
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
import java.util.List;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerifiableCertificationFactory {
    private final DefaultSignerConfig defaultSignerConfig;
    private final LEARCredentialEmployeeFactory learCredentialEmployeeFactory;
    private final ObjectMapper objectMapper;
    private final JWTService jwtService;
    private final RemoteSignatureConfig remoteSignatureConfig;

    public Mono<CredentialProcedureCreationRequest> mapAndBuildVerifiableCertification(JsonNode credential, String token, String operationMode) {
        VerifiableCertification verifiableCertification = objectMapper.convertValue(credential, VerifiableCertification.class);
        SignedJWT signedJWT = jwtService.parseJWT(token);
        String vcClaim = jwtService.getClaimFromPayload(signedJWT.getPayload(), "vc");
        LEARCredentialEmployee learCredentialEmployee = learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(vcClaim);
        return
                buildVerifiableCertification(verifiableCertification, learCredentialEmployee)
                .flatMap(verifiableCertificationDecoded ->
                        convertVerifiableCertificationInToString(verifiableCertificationDecoded)
                                .flatMap(decodedCredential ->
                                        buildCredentialProcedureCreationRequest(decodedCredential, verifiableCertificationDecoded, operationMode)
                                )
                );
    }
    //TODO el issuer del certification debe construirse como en el LEARCredentialEmployeeFactory una vez se realice la tarea de modificación del flujo para cumplir con el OIDC4VC
    private Mono<VerifiableCertification> buildVerifiableCertification(VerifiableCertification credential, LEARCredentialEmployee learCredentialEmployee) {
        //TODO repensar esto cuando el flujo del Verification cumpla con el OIDC4VC
        //Generate Issuer and Signer using LEARCredentialEmployee method


        // Compliance list with new IDs
        List<VerifiableCertification.CredentialSubject.Compliance> populatedCompliance = credential.credentialSubject().compliance().stream()
                .map(compliance -> VerifiableCertification.CredentialSubject.Compliance.builder()
                        .id(UUID.randomUUID().toString())
                        .hash(compliance.hash())
                        .scope(compliance.scope())
                        .standard(compliance.standard())
                        .build())
                .toList();

        // Create the Signer object using the retrieved UserDetails
        VerifiableCertification.Signer signer = VerifiableCertification.Signer.builder()
                .commonName(defaultSignerConfig.getCommonName())
                .country(defaultSignerConfig.getCountry())
                .emailAddress(defaultSignerConfig.getEmail())
                .organization(defaultSignerConfig.getOrganization())
                .organizationIdentifier(defaultSignerConfig.getOrganizationIdentifier())
                .serialNumber(defaultSignerConfig.getSerialNumber())
                .build();

        String issuerCred;
        if((remoteSignatureConfig.getRemoteSignatureType()).equals(SIGNATURE_REMOTE_TYPE_SERVER)){
            issuerCred = DID_ELSI + defaultSignerConfig.getOrganizationIdentifier();
        } else {
            issuerCred = DID_ELSI + "VATES-D70795026";
        }


        // Create the Issuer object using the retrieved UserDetails
        VerifiableCertification.Issuer issuer = VerifiableCertification.Issuer.builder()
                .commonName(defaultSignerConfig.getCommonName())
                .country(defaultSignerConfig.getCountry())
                .id(issuerCred)
                .organization(defaultSignerConfig.getOrganization())
                .build();

        VerifiableCertification.Atester atester = VerifiableCertification.Atester.builder()
                .firstName(learCredentialEmployee.credentialSubject().mandate().mandatee().firstName())
                .lastName(learCredentialEmployee.credentialSubject().mandate().mandatee().lastName())
                .country(learCredentialEmployee.credentialSubject().mandate().mandator().country())
                .id(learCredentialEmployee.credentialSubject().mandate().mandatee().id())
                .organization(learCredentialEmployee.credentialSubject().mandate().mandator().organization())
                .organizationIdentifier(learCredentialEmployee.credentialSubject().mandate().mandator().organizationIdentifier())
                .build();

        // Build the VerifiableCertification object
        return Mono.just(VerifiableCertification.builder()
                .context(CREDENTIAL_CONTEXT)
                .id(UUID.randomUUID().toString())
                .type(VERIFIABLE_CERTIFICATION_TYPE)
                .issuer(issuer)
                .credentialSubject(VerifiableCertification.CredentialSubject.builder()
                        .company(credential.credentialSubject().company())
                        .product(credential.credentialSubject().product())
                        .compliance(populatedCompliance)
                        .build())
                .validFrom(credential.validFrom())
                .validUntil(credential.validUntil())
                .signer(signer)
                .atester(atester)
                .build());
    }

    public Mono<VerifiableCertificationJwtPayload> buildVerifiableCertificationJwtPayload(VerifiableCertification credential){
        return Mono.just(
                VerifiableCertificationJwtPayload.builder()
                        .JwtId(UUID.randomUUID().toString())
                        .credential(credential)
                        .expirationTime(parseDateToUnixTime(credential.validUntil()))
                        .issuedAt(parseDateToUnixTime(credential.validFrom()))
                        .notValidBefore(parseDateToUnixTime(credential.validFrom()))
                        .issuer(credential.issuer().id())
                        .subject(credential.credentialSubject().product().productId())
                        .build()
        );
    }

    private long parseDateToUnixTime(String date) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        return zonedDateTime.toInstant().getEpochSecond();
    }

    public VerifiableCertification mapStringToVerifiableCertification(String learCredential)
            throws InvalidCredentialFormatException {
        try {
            log.info(objectMapper.readValue(learCredential, VerifiableCertification.class).toString());
            return objectMapper.readValue(learCredential, VerifiableCertification.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing VerifiableCertification", e);
            throw new InvalidCredentialFormatException("Error parsing VerifiableCertification");
        }
    }

    private Mono<String> convertVerifiableCertificationInToString(VerifiableCertification verifiableCertification) {
        try {

            return Mono.just(objectMapper.writeValueAsString(verifiableCertification));
        } catch (JsonProcessingException e) {
            throw new ParseErrorException(e.getMessage());
        }
    }

    public Mono<String> convertVerifiableCertificationJwtPayloadInToString(VerifiableCertificationJwtPayload verifiableCertificationJwtPayload) {
        try {
            return Mono.just(objectMapper.writeValueAsString(verifiableCertificationJwtPayload));
        } catch (JsonProcessingException e) {
            throw new ParseErrorException(e.getMessage());
        }
    }


    private Mono<CredentialProcedureCreationRequest> buildCredentialProcedureCreationRequest(String decodedCredential, VerifiableCertification verifiableCertificationDecoded, String operationMode) {
        String organizationId = defaultSignerConfig.getOrganizationIdentifier();
        return Mono.just(CredentialProcedureCreationRequest.builder()
                .credentialId(verifiableCertificationDecoded.id())
                .organizationIdentifier(organizationId)
                .credentialDecoded(decodedCredential)
                .credentialType(CredentialType.VERIFIABLE_CERTIFICATION)
                .subject(verifiableCertificationDecoded.credentialSubject().product().productName())
                .validUntil(parseEpochSecondIntoTimestamp(parseDateToUnixTime(verifiableCertificationDecoded.validUntil())))
                .operationMode(operationMode)
                .build()
        );
    }

    private Timestamp parseEpochSecondIntoTimestamp(Long unixEpochSeconds) {
        return Timestamp.from(Instant.ofEpochSecond(unixEpochSeconds));
    }
}
