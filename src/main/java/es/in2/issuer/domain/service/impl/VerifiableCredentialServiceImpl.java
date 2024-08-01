package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
import es.in2.issuer.domain.exception.CredentialTypeUnsupportedException;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.domain.service.VerifiableCredentialService;
import es.in2.issuer.domain.util.factory.CredentialFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.ParseException;

import static es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL_EMPLOYEE;
import static es.in2.issuer.domain.util.Constants.VERIFIABLE_CERTIFICATION;


@Service
@RequiredArgsConstructor
@Slf4j
public class VerifiableCredentialServiceImpl implements VerifiableCredentialService {
    private final ObjectMapper objectMapper;
    private final CredentialFactory credentialFactory;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;

    @Override
    public Mono<String> generateVc(String processId, String vcType, CredentialData credentialData) {
        return credentialFactory.mapCredentialIntoACredentialProcedureRequest(processId, vcType, credentialData.credential())
                .flatMap(credentialProcedureService::createCredentialProcedure)
                .flatMap(deferredCredentialMetadataService::createDeferredCredentialMetadata);

    }

    //    @Override
//    public Mono<String> generateVcPayLoad(String vcTemplate, String subjectDid, String issuerDid, String userData, Instant expiration) {
//        return Mono.fromCallable(() -> {
//            JsonNode vcTemplateNode = parseJson(vcTemplate);
//            String uuid = UUID.randomUUID().toString();
//            Instant nowInstant = Instant.now();
//
//            updateTemplateNode(vcTemplateNode, uuid, issuerDid, nowInstant, expiration);
//
//            JsonNode credentialSubjectValue = objectMapper.readTree(userData);
//            ((ObjectNode) credentialSubjectValue).put(ID, subjectDid);
//            ((ObjectNode) vcTemplateNode).set(CREDENTIAL_SUBJECT, credentialSubjectValue);
//
//            return objectMapper.writeValueAsString(constructFinalObjectNode(vcTemplateNode, subjectDid, issuerDid, uuid, nowInstant, expiration));
//        });
//    }

    @Override
    public Mono<VerifiableCredentialResponse> generateDeferredCredentialResponse(String processId, DeferredCredentialRequest deferredCredentialRequest) {
        return deferredCredentialMetadataService.getVcByTransactionId(deferredCredentialRequest.transactionId())
                .flatMap(deferredCredentialMetadataDeferredResponse -> {
                    if (deferredCredentialMetadataDeferredResponse.vc() != null) {
                        return credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(deferredCredentialMetadataDeferredResponse.procedureId())
                                .then(deferredCredentialMetadataService.deleteDeferredCredentialMetadataById(deferredCredentialMetadataDeferredResponse.id()))
                                .then(Mono.just(VerifiableCredentialResponse.builder()
                                        .credential(deferredCredentialMetadataDeferredResponse.vc())
                                        .build()));
                    } else {
                        return Mono.just(VerifiableCredentialResponse.builder()
                                .transactionId(deferredCredentialMetadataDeferredResponse.transactionId())
                                .build());
                    }
                });
    }

    @Override
    public Mono<Void> bindAccessTokenByPreAuthorizedCode(String processId, String accessToken, String preAuthCode) {
        try {
            JWSObject jwsObject = JWSObject.parse(accessToken);
            String newAuthServerNonce = jwsObject.getPayload().toJSONObject().get("jti").toString();
            return deferredCredentialMetadataService.updateAuthServerNonceByAuthServerNonce(newAuthServerNonce, preAuthCode);
        } catch (ParseException e){
            throw new RuntimeException();
        }

    }

    @Override
    public Mono<VerifiableCredentialResponse> buildCredentialResponse(String processId, String subjectDid, String authServerNonce, String format) {
        return deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
                .flatMap(procedureId -> {
                    log.info("Procedure ID obtained: " + procedureId);
                    return credentialProcedureService.getCredentialTypeByProcedureId(procedureId)
                            .flatMap(credentialType -> {
                                log.info("Credential Type obtained: " + credentialType);
                                return credentialProcedureService.getDecodedCredentialByProcedureId(procedureId)
                                        .flatMap(credential -> {
                                            log.info("Decoded Credential obtained: " + credential);
                                            return credentialFactory.mapCredentialBasedOnType(processId, credentialType, credential, subjectDid)
                                                    .flatMap(bindCredential -> {
                                                        log.info("Bind Credential obtained: " + bindCredential);
                                                        return credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindCredential, format)
                                                                .then(deferredCredentialMetadataService.updateDeferredCredentialMetadataByAuthServerNonce(authServerNonce, format)
                                                                        .flatMap(transactionId -> {
                                                                            log.info("Transaction ID obtained: " + transactionId);
                                                                            return castCredential(bindCredential)
                                                                                    .flatMap(credentialJson -> {
                                                                                        log.info("Credential JSON: " + credentialJson);
                                                                                        return Mono.just(VerifiableCredentialResponse.builder()
                                                                                                .credential(credentialJson)
                                                                                                .transactionId(transactionId)
                                                                                                .build());
                                                                                    })
                                                                                    .onErrorResume(e -> {
                                                                                        log.error("Error processing credential", e);
                                                                                        return Mono.error(e);
                                                                                    });
                                                                        }));
                                                    });
                                        });
                            });
                });
    }
    public Mono<String> castCredential(String credential) {
        try {
            JsonNode typesNode = objectMapper.readTree(credential).get("vc").get("type");
            JsonNode vcNode = objectMapper.readTree(credential).get("vc");

            if (typesNode != null && typesNode.isArray()) {
                for (JsonNode type : typesNode) {
                    String typeText = type.asText();
                    if (LEAR_CREDENTIAL_EMPLOYEE.equals(typeText)) {
                        LEARCredentialEmployee learCredential = objectMapper.treeToValue(vcNode, LEARCredentialEmployee.class);
                        return Mono.just(objectMapper.writeValueAsString(learCredential));
                    } else if (VERIFIABLE_CERTIFICATION.equals(typeText)) {
                        VerifiableCertification verifiableCertification = objectMapper.treeToValue(vcNode, VerifiableCertification.class);
                        return Mono.just(objectMapper.writeValueAsString(verifiableCertification));
                    }
                }
            }
            // Return error if no matching type is found
            return Mono.error(new CredentialTypeUnsupportedException("Unsupported credential type"));
        } catch (Exception e) {
            // Return error in case of exceptions
            return Mono.error(e);
        }
    }
//    private String castCredential (String credential) throws JsonProcessingException {
//        // Extract the credential types
//        JsonNode types = objectMapper.readTree(credential).get("vc").get("type");
//        // Extract the "vc" object
//        JsonNode vcNode = objectMapper.readTree(credential).get("vc");
//        if (types != null && types.isArray()) {
//            for (JsonNode type : types) {
//                if (type.asText().equals(LEAR_CREDENTIAL_EMPLOYEE)) {
//                    // Convert the "vc" object to LEARCredentialEmployee
//                    LEARCredentialEmployee learCredential = objectMapper.treeToValue(vcNode, LEARCredentialEmployee.class);
//                    // Convert LEARCredentialEmployee back to string
//                    return objectMapper.writeValueAsString(learCredential);
//                } else if (type.asText().equals(VERIFIABLE_CERTIFICATION)) {
//                    // Convert the "vc" object to VerifiableCertification
//                    VerifiableCertification verifiableCertification = objectMapper.treeToValue(vcNode, VerifiableCertification.class);
//                    // Convert VerifiableCertification back to string
//                    return objectMapper.writeValueAsString(verifiableCertification);
//                }
//            }
//        }
//    }

//    private void updateTemplateNode(JsonNode vcTemplateNode, String uuid, String issuerDid, Instant nowInstant, Instant expiration) {
//        ((ObjectNode) vcTemplateNode).put(ID, uuid);
//        ((ObjectNode) vcTemplateNode).put(ISSUER, issuerDid);
//        ((ObjectNode) vcTemplateNode).put(ISSUANCE_DATE, nowInstant.toString());
//        ((ObjectNode) vcTemplateNode).put(VALID_FROM, nowInstant.toString());
//        ((ObjectNode) vcTemplateNode).put(EXPIRATION_DATE, expiration.toString());
//    }
//
//    private ObjectNode constructFinalObjectNode(JsonNode vcTemplateNode, String subjectDid, String issuerDid, String uuid, Instant nowInstant, Instant expiration) {
//        ObjectNode finalObject = objectMapper.createObjectNode();
//        finalObject.put("sub", subjectDid);
//        finalObject.put("nbf", nowInstant.getEpochSecond());
//        finalObject.put("iss", issuerDid);
//        finalObject.put("exp", expiration.getEpochSecond());
//        finalObject.put("iat", nowInstant.getEpochSecond());
//        finalObject.put("jti", uuid);
//        finalObject.set("vc", vcTemplateNode);
//        return finalObject;
//    }
//    private JsonNode parseJson(String json) {
//        try {
//            return objectMapper.readTree(json);
//        } catch (JsonProcessingException e) {
//            throw new ParseErrorException(e.getMessage());
//        }
//    }
}
