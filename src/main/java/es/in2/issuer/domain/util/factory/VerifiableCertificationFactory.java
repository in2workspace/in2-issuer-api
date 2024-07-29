package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.in2.issuer.domain.exception.ParseErrorException;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.service.AccessTokenService;
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

    private final ObjectMapper objectMapper;
    private final AccessTokenService accessTokenService;

    public Mono<String> mapCredential(String credential) {
        VerifiableCertificationJwtPayload baseLearCredentialEmployee = mapStringToVerifiableCertification(credential);
        return Mono.just(baseLearCredentialEmployee)
                .flatMap(this::convertVerifiableCertificationInToString);
    }

    public Mono<CredentialProcedureCreationRequest> mapAndBuildVerifiableCertification(JsonNode credential) {
        return Mono.fromCallable(() -> setIssuerAndSigner(credential))
                .flatMap(this::buildVerifiableCertificationJwtPayload)
                .flatMap(verifiableCertificationJwtPayload ->
                        convertVerifiableCertificationInToString(verifiableCertificationJwtPayload)
                                .flatMap(decodedCredential ->
                                        buildCredentialProcedureCreationRequest(decodedCredential, verifiableCertificationJwtPayload)
                                )
                );
    }

    public JsonNode setIssuerAndSigner(JsonNode credential) {
        try {
            // Create a copy of the original JsonNode to avoid mutating the input
            JsonNode modifiedCredential = objectMapper.readTree(credential.toString());
            ObjectNode objectNode = (ObjectNode) modifiedCredential;

            // todo extraer los datos del signer de las configuraciones

            // Replace the "issuer"
            //objectNode.put("issuer", DID_ELSI + "VATEU-B99999999");

            // Access the issuer object node
            ObjectNode issuerNode = (ObjectNode) modifiedCredential.get("issuer");

            // Set new values for commonName and country
            issuerNode.put("commonName", "ZEUS OLIMPOS");
            issuerNode.put("country", "EU");
            issuerNode.put("id", DID_ELSI + "VATEU-B99999999");
            issuerNode.put("organization", "IN2");

            // Update issuer in the credential
            objectNode.set("issuer", issuerNode);

            // Create the Signer object
            Signer signer = Signer.builder()
                    .commonName("ZEUS OLIMPOS")
                    .country("EU")
                    .emailAddress("domesupport@in2.es")
                    .organization("IN2")
                    .organizationIdentifier("VATEU-BXXXXXXXX")
                    .serialNumber("IDCEU-XXXXXXXXP")
                    .build();

            // Convert Signer object to JsonNode
            JsonNode signerNode = objectMapper.valueToTree(signer);

            // Add the signer object to the credential
            objectNode.set("signer", signerNode);

            return modifiedCredential;
        } catch (JsonProcessingException e) {
            throw new ParseErrorException(e.getMessage());
        }
    }

    private VerifiableCertificationJwtPayload mapStringToVerifiableCertification(String credential) {
        try {
            log.info(objectMapper.readValue(credential, VerifiableCertificationJwtPayload.class).toString());
            return objectMapper.readValue(credential, VerifiableCertificationJwtPayload.class);
        } catch (JsonProcessingException e) {
            throw new ParseErrorException(e.getMessage());
        }
    }

    private Mono<VerifiableCertificationJwtPayload> buildVerifiableCertificationJwtPayload(JsonNode credential){
        return Mono.just(
                VerifiableCertificationJwtPayload.builder()
                        .JwtId(UUID.randomUUID().toString())
                        .credential(credential)
                        .expirationTime(parseDateToUnixTime(credential.get("expirationDate").asText()))
                        .issuedAt(parseDateToUnixTime(credential.get("issuanceDate").asText()))
                        .notValidBefore(parseDateToUnixTime(credential.get("validFrom").asText()))
                        .issuer(credential.get("issuer").get("id").asText())
                        .subject(credential.get("credentialSubject").get("product").get("productId").asText())
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
        // todo extraer el organization id de las configuraciones
        String organizationId = "VATEU-B99999999";

        return Mono.just(
                CredentialProcedureCreationRequest.builder()
                        .credentialId(verifiableCertificationJwtPayload.credential().get("id").asText())
                        .organizationIdentifier(organizationId)
                        .credentialDecoded(decodedCredential)
                        .build()
        );
    }
//    private Mono<CredentialProcedureCreationRequest> buildCredentialProcedureCreationRequest(String decodedCredential, VerifiableCertificationJwtPayload verifiableCertificationJwtPayload) {
//        return accessTokenService.getOrganizationIdFromCurrentSession()
//                .flatMap(organizationId ->
//                        Mono.just(
//                                CredentialProcedureCreationRequest.builder()
//                                        .credentialId(verifiableCertificationJwtPayload.credential().get("id").asText())
//                                        .organizationIdentifier(organizationId)
//                                        .credentialDecoded(decodedCredential)
//                                        .build()
//                        )
//                );
//    }

}
