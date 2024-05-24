package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.dto.DeferredCredentialRequest;
import es.in2.issuer.domain.model.dto.LEARCredentialRequest;
import es.in2.issuer.domain.model.dto.VerifiableCredentialResponse;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.domain.service.VerifiableCredentialService;
import es.in2.issuer.domain.util.factory.CredentialFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
@RequiredArgsConstructor
@Slf4j
public class VerifiableCredentialServiceImpl implements VerifiableCredentialService {
    private final ObjectMapper objectMapper;
    private final CredentialFactory credentialFactory;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;

    @Override
    public Mono<String> generateVc(String processId, String vcType, LEARCredentialRequest learCredentialRequest) {
        return credentialFactory.mapCredentialIntoACredentialProcedureRequest(processId, vcType, learCredentialRequest.credential())
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
    public Mono<String> generateDeferredCredentialResponse(String processId, DeferredCredentialRequest deferredCredentialRequest) {
        return null;
    }

    @Override
    public Mono<Void> bindAccessTokenByPreAuthorizedCode(String processId, String accessToken, String preAuthCode) {
        return deferredCredentialMetadataService.updateAuthServerNonceByAuthServerNonce(accessToken, preAuthCode);
    }

    @Override
    public Mono<VerifiableCredentialResponse> buildCredentialResponse(String processId, String subjectDid, String accessToken, String format) {
        return deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(accessToken)
                .flatMap(procedureId -> credentialProcedureService.getCredentialTypeByProcedureId(procedureId)
                        .flatMap(credentialType -> credentialProcedureService.getDecodedCredentialByProcedureId(procedureId)
                                .flatMap(credential -> credentialFactory.mapCredentialAndBindMandateeId(processId, credentialType, credential, subjectDid))
                                .flatMap(bindCredential -> credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindCredential)
                                        .then(deferredCredentialMetadataService.updateDeferredCredentialMetadataByAuthServerNonce(accessToken, format)
                                                .flatMap(transactionId -> Mono.just(VerifiableCredentialResponse.builder()
                                                                .credential(bindCredential)
                                                                .transactionId(transactionId)
                                                                .build()
                                                        )
                                                )
                                        )
                                )));
    }

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
