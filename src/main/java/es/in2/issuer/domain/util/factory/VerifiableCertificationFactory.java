package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.ParseErrorException;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.infrastructure.config.DefaultIssuerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.DID_ELSI;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerifiableCertificationFactory {

    private final DefaultIssuerConfig defaultIssuerConfig;
    private final ObjectMapper objectMapper;

    public Mono<String> mapCredential(String credential) {
        VerifiableCertificationJwtPayload baseLearCredentialEmployee = mapStringToVerifiableCertification(credential);
        return Mono.just(baseLearCredentialEmployee)
                .flatMap(this::convertVerifiableCertificationInToString);
    }

    public Mono<CredentialProcedureCreationRequest> mapAndBuildVerifiableCertification(JsonNode credential) {
        VerifiableCertification verifiableCertification = objectMapper.convertValue(credential, VerifiableCertification.class);

        return buildVerifiableCertification(verifiableCertification)
                .flatMap(this::buildVerifiableCertificationJwtPayload)
                .flatMap(verifiableCertificationJwtPayload ->
                        convertVerifiableCertificationInToString(verifiableCertificationJwtPayload)
                                .flatMap(decodedCredential ->
                                        buildCredentialProcedureCreationRequest(decodedCredential, verifiableCertificationJwtPayload)
                                )
                );
    }


    public Mono<VerifiableCertification> buildVerifiableCertification(VerifiableCertification credential) {
            // Create the Issuer object
            VerifiableCertification.Issuer issuer = VerifiableCertification.Issuer.builder()
                    .commonName(defaultIssuerConfig.getCommonName())
                    .country(defaultIssuerConfig.getCountry())
                    .id(DID_ELSI + defaultIssuerConfig.getOrganizationIdentifier())
                    .organization(defaultIssuerConfig.getOrganization())
                    .build();

            // Create the Signer object
            VerifiableCertification.Signer signer = VerifiableCertification.Signer.builder()
                    .commonName(defaultIssuerConfig.getCommonName())
                    .country(defaultIssuerConfig.getCountry())
                    .emailAddress(defaultIssuerConfig.getEmail())
                    .organization(defaultIssuerConfig.getOrganization())
                    .organizationIdentifier(defaultIssuerConfig.getOrganizationIdentifier())
                    .serialNumber(defaultIssuerConfig.getSerialNumber())
                    .build();

            return Mono.just(VerifiableCertification.builder()
                    .id(credential.id())
                    .type(credential.type())
                    .issuer(issuer)
                    .credentialSubject(credential.credentialSubject())
                    .issuanceDate(credential.issuanceDate())
                    .validFrom(credential.validFrom())
                    .expirationDate(credential.expirationDate())
                    .signer(signer)
                    .build());
    }

    private VerifiableCertificationJwtPayload mapStringToVerifiableCertification(String credential) {
        try {
            log.info(objectMapper.readValue(credential, VerifiableCertificationJwtPayload.class).toString());
            return objectMapper.readValue(credential, VerifiableCertificationJwtPayload.class);
        } catch (JsonProcessingException e) {
            throw new ParseErrorException(e.getMessage());
        }
    }

    private Mono<VerifiableCertificationJwtPayload> buildVerifiableCertificationJwtPayload(VerifiableCertification credential){
        return Mono.just(
                VerifiableCertificationJwtPayload.builder()
                        .JwtId(UUID.randomUUID().toString())
                        .credential(credential)
                        .expirationTime(parseDateToUnixTime(credential.expirationDate()))
                        .issuedAt(parseDateToUnixTime(credential.issuanceDate()))
                        .notValidBefore(parseDateToUnixTime(credential.validFrom()))
                        .issuer(credential.issuer().id())
                        .subject(credential.credentialSubject().product().productId())
                        .build()
        );
    }

//    private long parseDateToUnixTime(String date) {
//        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, DateTimeFormatter.ISO_ZONED_DATE_TIME);
//        return zonedDateTime.toInstant().getEpochSecond();
//    }

    private long parseDateToUnixTime(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSSSS X 'UTC'");
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date, formatter.withZone(ZoneOffset.UTC));
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
        return Mono.just(
                CredentialProcedureCreationRequest.builder()
                        .credentialId(verifiableCertificationJwtPayload.credential().id())
                        .organizationIdentifier(defaultIssuerConfig.getOrganizationIdentifier())
                        .credentialDecoded(decodedCredential)
                        .build()
        );
    }

}
