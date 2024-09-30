package es.in2.issuer.application.workflow.impl;

import com.nimbusds.jose.JWSObject;
import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.domain.exception.CredentialTypeUnsupportedException;
import es.in2.issuer.domain.exception.InvalidOrMissingProofException;
import es.in2.issuer.domain.exception.ParseErrorException;
import es.in2.issuer.domain.exception.ResponseUriException;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.infrastructure.config.AppConfig;
import es.in2.issuer.infrastructure.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.naming.OperationNotSupportedException;
import java.text.ParseException;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifiableCredentialIssuanceWorkflowImpl implements VerifiableCredentialIssuanceWorkflow {


    private final RemoteSignatureService remoteSignatureService;
    private final VerifiableCredentialService verifiableCredentialService;
    private final AppConfig appConfig;
    private final ProofValidationService proofValidationService;
    private final EmailService emailService;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;
    private final CredentialSignerWorkflow credentialSignerWorkflow;
    private final WebClientConfig webClient;

//    @Override
//    public Mono<Void> completeIssuanceCredentialProcess(String processId, String type, IssuanceRequest issuanceRequest) {
//        return verifiableCredentialService.generateVc(processId, type, issuanceRequest)
//                .flatMap(transactionCode -> {
//                    String email = issuanceRequest.payload().get("mandatee").get("email").asText();
//                    String firstName =  issuanceRequest.payload().get("mandatee").get("first_name").asText();
//                    return emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, firstName,appConfig.getWalletUrl());
//                });
//    }

    @Override
    public Mono<Void> completeIssuanceCredentialProcess(String processId, String type, IssuanceRequest issuanceRequest, String token) {
        if (issuanceRequest.operationMode().equals(ASYNC)) {
            if(issuanceRequest.schema().equals(LEAR_CREDENTIAL_EMPLOYEE)){
                return verifiableCredentialService.generateVc(processId, type, issuanceRequest)
                        .flatMap(transactionCode -> sendCredentialOfferEmail(type, transactionCode, issuanceRequest));
            }
            return Mono.error(new CredentialTypeUnsupportedException(type));
        } else if (issuanceRequest.operationMode().equals(SYNC)) {
                        if (issuanceRequest.schema().equals(VERIFIABLE_CERTIFICATION)) {
                            return verifiableCredentialService.generateVerifiableCertification(processId, type, issuanceRequest)
                                    .flatMap(procedureId -> credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(token, procedureId, JWT_VC))
                                    .flatMap(encodedVc -> sendVcToResponseUri(issuanceRequest.responseUri(), encodedVc, token));
                        } else if (issuanceRequest.schema().equals(LEAR_CREDENTIAL_EMPLOYEE)) {
                            return verifiableCredentialService.generateVc(processId, type, issuanceRequest)
                                    .flatMap(transactionCode -> sendCredentialOfferEmail(type, transactionCode, issuanceRequest));
                        }
                        return Mono.error(new CredentialTypeUnsupportedException(type));
        }
        return Mono.error(new OperationNotSupportedException("operation_mode: "+issuanceRequest.operationMode()));
    }

    private Mono<Void> sendCredentialOfferEmail(String type, String transactionCode, IssuanceRequest issuanceRequest){
        if (LEAR_CREDENTIAL_EMPLOYEE.equals(type)) {
            String email = issuanceRequest.payload().get("mandatee").get("email").asText();
            String name = issuanceRequest.payload().get("mandatee").get("first_name").asText();
            return emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, name, appConfig.getWalletUrl());
        } else if (VERIFIABLE_CERTIFICATION.equals(type)) {
            // Envío de email para VerifiableCertification
//            String email = credentialData.payload().get("credentialSubject").get("company").get("email").asText();
//            String name = credentialData.payload().get("credentialSubject").get("company").get("commonName").asText();
//            return emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, name, appConfig.getWalletUrl());
            return Mono.empty();
        } else {
            return Mono.error(new CredentialTypeUnsupportedException(type));
        }
    }

    private Mono<Void> sendVcToResponseUri(String responseUri, String encodedVc, String token) {
//        ResponseUriRequest responseUriRequest = ResponseUriRequest.builder()
//                .encodedVc(encodedVc)
//                .build();

//        return webClient.commonWebClient()
//                .patch()
//                .uri(responseUri)
//                .contentType(MediaType.APPLICATION_JSON)
//                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
//                .bodyValue(responseUriRequest)
//                .exchangeToMono(response -> {
//                    if (response.statusCode().is2xxSuccessful()) {
//                        return Mono.empty();
//                    } else {
//                        return Mono.error(new ResponseUriException("Error while sending VC to response URI, error: " + response.statusCode()));
//                    }
//                });
        log.info("Se envio a esta uri: {} la credencial: {}", responseUri, encodedVc);
        return Mono.empty();
    }

    @Override
    public Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(
            String processId,
            CredentialRequest credentialRequest,
            String token
    ) {
        try {
            JWSObject jwsObject = JWSObject.parse(token);
            String authServerNonce = jwsObject.getPayload().toJSONObject().get("jti").toString();

            return proofValidationService.isProofValid(credentialRequest.proof().jwt(), token)
                    .flatMap(isValid -> {
                        if (Boolean.FALSE.equals(isValid)) {
                            return Mono.error(new InvalidOrMissingProofException("Invalid proof"));
                        } else {
                            return extractDidFromJwtProof(credentialRequest.proof().jwt());
                        }
                    })
                    .flatMap(subjectDid ->
                            deferredCredentialMetadataService.getOperationModeByAuthServerNonce(authServerNonce)
                                    .flatMap(operationMode ->
                                            verifiableCredentialService.buildCredentialResponse(processId, subjectDid, authServerNonce, credentialRequest.format(), token, operationMode)
                                                    .flatMap(credentialResponse -> {
                                                                if(operationMode.equals(ASYNC)){
                                                                    return deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
                                                                            .flatMap(credentialProcedureService::getSignerEmailFromDecodedCredentialByProcedureId)
                                                                            .flatMap(email ->
                                                                                    emailService.sendPendingCredentialNotification(email, "Pending Credential")
                                                                                            .then(Mono.just(credentialResponse))
                                                                            );
                                                                } else if (operationMode.equals(SYNC)) {
                                                                    return deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
                                                                            .flatMap(credentialProcedureService::updateCredentialProcedureCredentialStatusToValidByProcedureId)
                                                                            .then(deferredCredentialMetadataService.deleteDeferredCredentialMetadataByAuthServerNonce(authServerNonce))
                                                                            .then(Mono.just(credentialResponse));
                                                                } else {
                                                                    return Mono.error(new IllegalArgumentException("Unknown operation mode: " + operationMode));
                                                                }
                                                            }
                                                    )
                                    )
                    );
        } catch (ParseException e) {
            log.error("Error parsing the accessToken", e);
            throw new ParseErrorException("Error parsing accessToken");
        }
    }

    @Override
    public Mono<Void> bindAccessTokenByPreAuthorizedCode(String processId, AuthServerNonceRequest authServerNonceRequest) {
        return verifiableCredentialService.bindAccessTokenByPreAuthorizedCode
                (processId, authServerNonceRequest.accessToken(), authServerNonceRequest.preAuthorizedCode());
    }

    @Override
    public Mono<BatchCredentialResponse> generateVerifiableCredentialBatchResponse(
            String username,
            BatchCredentialRequest batchCredentialRequest,
            String token
    ) {
        return Flux.fromIterable(batchCredentialRequest.credentialRequests())
                .flatMap(credentialRequest -> generateVerifiableCredentialResponse(username, credentialRequest, token)
                        .map(verifiableCredentialResponse -> new BatchCredentialResponse.CredentialResponse(verifiableCredentialResponse.credential())))
                .collectList()
                .map(BatchCredentialResponse::new);
    }

    @Override
    public Mono<VerifiableCredentialResponse> generateVerifiableCredentialDeferredResponse(String processId, DeferredCredentialRequest deferredCredentialRequest) {
                return verifiableCredentialService.generateDeferredCredentialResponse(processId,deferredCredentialRequest)
                .onErrorResume(e -> Mono.error(new RuntimeException("Failed to process the credential for the next processId: " + processId, e)));
    }

    @Override
    public Mono<Void> signDeferredCredential(String unsignedCredential, String userId, UUID credentialId, String token) {
        return null;
//            return verifiableCredentialService.generateDeferredCredentialResponse(unsignedCredential)
//                    .flatMap(vcPayload -> {
//                        SignatureRequest signatureRequest = SignatureRequest.builder()
//                                .configuration(SignatureConfiguration.builder().type(SignatureType.JADES).parameters(Collections.emptyMap()).build())
//                                .data(vcPayload)
//                                .build();
//                        return remoteSignatureService.sign(signatureRequest, token)
//                                .publishOn(Schedulers.boundedElastic())
//                                .map(SignedData::data);
//                    })
//                    .flatMap(signedCredential -> credentialManagementService.updateCredential(signedCredential, credentialId, userId))
//                    .onErrorResume(e -> Mono.error(new RuntimeException("Failed to sign and update the credential.", e)));
    }

    private Mono<String> extractDidFromJwtProof(String jwtProof) {
        return Mono.fromCallable(() -> {
            JWSObject jwsObject = JWSObject.parse(jwtProof);
            // Extract the issuer DID from the kid claim in the header
            String kid = jwsObject.getHeader().toJSONObject().get("kid").toString();
            // Split the kid string at '#' and take the first part
            return kid.split("#")[0];
        });
    }

}
