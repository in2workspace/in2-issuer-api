package es.in2.issuer.application.workflow.impl;

import com.nimbusds.jose.JWSObject;
import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.domain.exception.*;
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

import java.text.ParseException;

import static es.in2.issuer.domain.util.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerifiableCredentialIssuanceWorkflowImpl implements VerifiableCredentialIssuanceWorkflow {

    private final VerifiableCredentialService verifiableCredentialService;
    private final AppConfig appConfig;
    private final ProofValidationService proofValidationService;
    private final EmailService emailService;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;
    private final CredentialSignerWorkflow credentialSignerWorkflow;
    private final WebClientConfig webClient;

    @Override
    public Mono<Void> completeWithdrawCredentialProcess(String processId, String type, CredentialData credentialData, String token) {
        if (credentialData.operationMode()==null || credentialData.operationMode().equals(SYNC)) {
            return verifiableCredentialService.generateVc(processId, type, credentialData)
                    .flatMap(transactionCode -> sendCredentialOfferEmail(type, transactionCode, credentialData));
        } else if (credentialData.operationMode().equals(ASYNC)) {
            return verifiableCredentialService.generateVc(processId, type, credentialData)
                    .flatMap(transactionCode ->
                            deferredCredentialMetadataService.getProcedureIdByTransactionCode(transactionCode)
                                    .flatMap(procedureId -> credentialSignerWorkflow.signAndUpdateCredential(token, procedureId))
                                    //TODO: envio de credential offer al email para emision asíncrona
//                                    .flatMap(signedCredential -> sendCredentialOfferEmail(type, transactionCode, credentialData)
//                                            .thenReturn(signedCredential))
                                    .flatMap(encodedVc -> {
                                        if (credentialData.responseUri() != null) {
                                            return Mono.error(new OperationNotSupportedException("response_uri is not supported"));
                                            //TODO: envio de la VC al response_uri
                                            //return sendVcToResponseUri(credentialData.responseUri(), encodedVc, token);
                                        } else {
                                            return Mono.empty();
                                        }
                                    })
                    );
        } else {
            return Mono.error(new CredentialTypeUnsupportedException(type));
        }
    }

    private Mono<Void> sendCredentialOfferEmail(String type, String transactionCode, CredentialData credentialData){
        String email = credentialData.payload().get("mandatee").get("email").asText();
        String name = credentialData.payload().get("mandatee").get("first_name").asText();
        return emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, name, appConfig.getWalletUrl());
        //TODO: envio de credential offer al email condicional al tipo de VC
//        if (LEAR_CREDENTIAL_EMPLOYEE.equals(type)) {
//            String email = credentialData.credential().get("mandatee").get("email").asText();
//            String name = credentialData.credential().get("mandatee").get("first_name").asText();
//            return emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, name, appConfig.getWalletUrl());
//        } else if (VERIFIABLE_CERTIFICATION.equals(type)) {
//            String email = credentialData.payload().get("credentialSubject").get("company").get("email").asText();
//            String name = credentialData.payload().get("credentialSubject").get("company").get("commonName").asText();
//            return emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, name, appConfig.getWalletUrl());
//        } else {
//            return Mono.error(new CredentialTypeUnsupportedException(type));
//        }
    }

    private Mono<Void> sendVcToResponseUri(String responseUri, String encodedVc, String token) {
        ResponseUriRequest responseUriRequest = ResponseUriRequest.builder()
                .encodedVc(encodedVc)
                .build();

        return webClient.commonWebClient()
                .patch()
                .uri(responseUri)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                .bodyValue(responseUriRequest)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.empty();
                    } else {
                        return Mono.error(new ResponseUriException("Error while sending VC to response URI, error: " + response.statusCode()));
                    }
                });
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
                    .flatMap(subjectDid -> verifiableCredentialService.buildCredentialResponse(processId, subjectDid, authServerNonce, credentialRequest.format())
                            .flatMap(credentialResponse -> deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
                                    .flatMap(credentialProcedureService::getSignerEmailFromDecodedCredentialByProcedureId)
                                    .flatMap(email -> emailService.sendPendingCredentialNotification(email,"Pending Credential")
                                            .then(Mono.just(credentialResponse)))));
        }
        catch (ParseException e){
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
