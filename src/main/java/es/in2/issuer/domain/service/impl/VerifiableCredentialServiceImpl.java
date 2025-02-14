package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.domain.model.dto.DeferredCredentialRequest;
import es.in2.issuer.domain.model.dto.PreSubmittedCredentialRequest;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployee;
import es.in2.issuer.domain.model.dto.VerifiableCredentialResponse;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.domain.service.VerifiableCredentialService;
import es.in2.issuer.domain.util.factory.CredentialFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.ParseException;

import static es.in2.issuer.domain.util.Constants.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class VerifiableCredentialServiceImpl implements VerifiableCredentialService {
    private final ObjectMapper objectMapper;
    private final CredentialFactory credentialFactory;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;
    private final CredentialSignerWorkflow credentialSignerWorkflow;

    @Override
    public Mono<String> generateVc(String processId, String vcType, PreSubmittedCredentialRequest preSubmittedCredentialRequest, String token) {
        log.info("preSubmittedCredentialRequest mode {}", preSubmittedCredentialRequest.operationMode());
        return credentialFactory.mapCredentialIntoACredentialProcedureRequest(processId, vcType, preSubmittedCredentialRequest, token)
                .flatMap(credentialProcedureService::createCredentialProcedure)
                .flatMap(procedureId -> deferredCredentialMetadataService.createDeferredCredentialMetadata(
                        procedureId,
                        preSubmittedCredentialRequest.operationMode(),
                        preSubmittedCredentialRequest.responseUri()));
    }

    @Override
    public Mono<String> generateVerifiableCertification(String processId, String vcType, PreSubmittedCredentialRequest preSubmittedCredentialRequest, String token) {
        return credentialFactory.mapCredentialIntoACredentialProcedureRequest(processId, vcType, preSubmittedCredentialRequest, token)
                .flatMap(credentialProcedureService::createCredentialProcedure);
    }

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
    public Mono<VerifiableCredentialResponse> buildCredentialResponse(String processId, String subjectDid, String authServerNonce, String format, String token, String operationMode) {

        return deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
                .flatMap(procedureId -> {
                    log.info("Procedure ID obtained: {}", procedureId);
                    return credentialProcedureService.getCredentialTypeByProcedureId(procedureId)
                            .flatMap(credentialType -> {
                                log.info("Credential Type obtained: {}", credentialType);
                                return credentialProcedureService.getDecodedCredentialByProcedureId(procedureId)
                                        .flatMap(credential -> {
                                            log.info("Decoded Credential obtained: {}", credential);
                                            return credentialFactory.mapCredentialAndBindMandateeId(processId, credentialType, credential, subjectDid)
                                                    .flatMap(bindCredential -> {
                                                        log.info("Bind Credential obtained: {}", bindCredential);
                                                        return credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindCredential, format)
                                                                .then(deferredCredentialMetadataService.updateDeferredCredentialMetadataByAuthServerNonce(authServerNonce, format))
                                                                .flatMap(transactionId -> {
                                                                    log.info("Transaction ID obtained: {}", transactionId);
                                                                    return buildCredentialResponseBasedOnOperationMode(operationMode, bindCredential, transactionId, authServerNonce, token);
                                                                });
                                                    });
                                        });
                            });
                });
    }

    private Mono<VerifiableCredentialResponse> buildCredentialResponseBasedOnOperationMode(String operationMode, String bindCredential, String transactionId, String authServerNonce, String token) {
        if (operationMode.equals(ASYNC)) {
            try {
                // Extract the "vc" object
                JsonNode vcNode = objectMapper.readTree(bindCredential).get(VC);
                // Convert the "jwtCredential" object to LEARCredentialEmployee
                LEARCredentialEmployee learCredential = objectMapper.treeToValue(vcNode, LEARCredentialEmployee.class);
                // Convert LEARCredentialEmployee back to string
                String bindLearCredentialJson = objectMapper.writeValueAsString(learCredential);

                log.info("LEAR Credential JSON: {}", bindLearCredentialJson);
                return Mono.just(VerifiableCredentialResponse.builder()
                        .credential(bindLearCredentialJson)
                        .transactionId(transactionId)
                        .build());
            } catch (JsonProcessingException e) {
                log.error("Error processing JSON", e);
                return Mono.error(e);
            }
        } else if (operationMode.equals(SYNC)) {
            return deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
                    .flatMap(procedureId -> credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(BEARER_PREFIX + token, procedureId, JWT_VC))
                    .flatMap(signedCredential -> Mono.just(VerifiableCredentialResponse.builder()
                            .credential(signedCredential)
                            .build())
                    );
        } else {
            return Mono.error(new IllegalArgumentException("Unknown operation mode: " + operationMode));
        }
    }

}
