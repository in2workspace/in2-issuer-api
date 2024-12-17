package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.ParseErrorException;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployee;
import es.in2.issuer.domain.model.dto.VerifiableCertification;
import es.in2.issuer.domain.model.dto.VerifiableCertificationJwtPayload;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.infrastructure.config.DefaultSignerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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

    public Mono<CredentialProcedureCreationRequest> mapAndBuildVerifiableCertification(JsonNode credential, String token) {
        VerifiableCertification verifiableCertification = objectMapper.convertValue(credential, VerifiableCertification.class);
        SignedJWT signedJWT = jwtService.parseJWT(token);
        String vcClaim = jwtService.getClaimFromPayload(signedJWT.getPayload(), "vc");
        LEARCredentialEmployee learCredentialEmployee = learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(vcClaim);
        return
                buildVerifiableCertification(verifiableCertification, learCredentialEmployee)
                .flatMap(this::buildVerifiableCertificationJwtPayload)
                .flatMap(verifiableCertificationJwtPayload ->
                        convertVerifiableCertificationInToString(verifiableCertificationJwtPayload)
                                .flatMap(decodedCredential ->
                                        buildCredentialProcedureCreationRequest(decodedCredential, verifiableCertificationJwtPayload)
                                )
                );
    }


    private Mono<VerifiableCertification> buildVerifiableCertification(VerifiableCertification credential, LEARCredentialEmployee learCredentialEmployee) {
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

        // Create the Issuer object using the retrieved UserDetails
        VerifiableCertification.Issuer issuer = VerifiableCertification.Issuer.builder()
                .commonName(defaultSignerConfig.getCommonName())
                .country(defaultSignerConfig.getCountry())
                .id(DID_ELSI + defaultSignerConfig.getOrganizationIdentifier())
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

    private Mono<VerifiableCertificationJwtPayload> buildVerifiableCertificationJwtPayload(VerifiableCertification credential){
        return Mono.just(
                VerifiableCertificationJwtPayload.builder()
                        .JwtId(UUID.randomUUID().toString())
                        .credential(credential)
                        .expirationTime(parseDateToUnixTime(credential.validUntil()))
                        .issuedAt(parseDateToUnixTime(credential.validFrom()))
                        .notValidBefore(parseDateToUnixTime(credential.validFrom()))
                        .issuer(DID_ELSI + credential.signer().organizationIdentifier())
                        .subject(credential.credentialSubject().product().productId())
                        .build()
        );
    }

    private long parseDateToUnixTime(String date) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, DateTimeFormatter.ISO_ZONED_DATE_TIME);
        return zonedDateTime.toInstant().getEpochSecond();
    }

    private Mono<String> convertVerifiableCertificationInToString(VerifiableCertificationJwtPayload verifiableCertificationJwtPayload) {
        try {

            return Mono.just(objectMapper.writeValueAsString(verifiableCertificationJwtPayload));
        } catch (JsonProcessingException e) {
            throw new ParseErrorException(e.getMessage());
        }
    }

    private Mono<CredentialProcedureCreationRequest> buildCredentialProcedureCreationRequest(String decodedCredential, VerifiableCertificationJwtPayload verifiableCertificationJwtPayload) {
        String organizationId = defaultSignerConfig.getOrganizationIdentifier();
        return Mono.just(CredentialProcedureCreationRequest.builder()
                .credentialId(verifiableCertificationJwtPayload.credential().id())
                .organizationIdentifier(organizationId)
                .credentialDecoded(decodedCredential)
                .build()
        );
    }
}
