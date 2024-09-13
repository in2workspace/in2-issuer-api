package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class VerifiableCredentialServiceImpl implements VerifiableCredentialService {
    private final ObjectMapper objectMapper;
    private final CredentialFactory credentialFactory;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;

    @Override
    public Mono<String> generateVc(String processId, String vcType, IssuanceRequest issuanceRequest) {
        return credentialFactory.mapCredentialIntoACredentialProcedureRequest(processId, vcType, issuanceRequest.payload())
                .flatMap(credentialProcedureService::createCredentialProcedure)
                .flatMap(deferredCredentialMetadataService::createDeferredCredentialMetadata);
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
                                            return credentialFactory.mapCredentialAndBindMandateeId(processId, credentialType, credential, subjectDid)
                                                    .flatMap(bindCredential -> {
                                                        log.info("Bind Credential obtained: " + bindCredential);
                                                        return credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindCredential, format)
                                                                .then(deferredCredentialMetadataService.updateDeferredCredentialMetadataByAuthServerNonce(authServerNonce, format)
                                                                        .flatMap(transactionId -> {
                                                                            log.info("Transaction ID obtained: " + transactionId);

                                                                            try {
                                                                                // Extract the "vc" object
                                                                                JsonNode vcNode = objectMapper.readTree(bindCredential).get("vc");
                                                                                // Convert the "vc" object to LEARCredentialEmployee
                                                                                LEARCredentialEmployee learCredential = objectMapper.treeToValue(vcNode, LEARCredentialEmployee.class);
                                                                                // Convert LEARCredentialEmployee back to string
                                                                                String bindLearCredentialJson = objectMapper.writeValueAsString(learCredential);

                                                                                log.info("LEAR Credential JSON: " + bindLearCredentialJson);
                                                                                return Mono.just(VerifiableCredentialResponse.builder()
                                                                                        .credential(bindLearCredentialJson)
                                                                                        .transactionId(transactionId)
                                                                                        .build());
                                                                            } catch (JsonProcessingException e) {
                                                                                log.error("Error processing JSON", e);
                                                                                return Mono.error(e);
                                                                            }
                                                                        }));
                                                    });
                                        });
                            });
                });
    }

}
