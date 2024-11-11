package es.in2.issuer.application.workflow.impl;

import com.nimbusds.jose.JWSObject;
import es.in2.issuer.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.domain.exception.InvalidOrMissingProofException;
import es.in2.issuer.domain.exception.ParseErrorException;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.domain.util.factory.LEARCredentialEmployeeFactory;
import es.in2.issuer.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    private final TrustFrameworkService trustFrameworkService;
    private final LEARCredentialEmployeeFactory credentialEmployeeFactory;

    @Override
    public Mono<Void> completeIssuanceCredentialProcess(String processId, String type, IssuanceRequest issuanceRequest) {
        return verifiableCredentialService.generateVc(processId, type, issuanceRequest)
                .flatMap(transactionCode -> {
                    String email = issuanceRequest.payload().get("mandatee").get("email").asText();
                    String firstName = issuanceRequest.payload().get("mandatee").get("first_name").asText();
                    return emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, firstName, appConfig.getWalletUrl());
                });
    }

    @Override
    public Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(String processId,
                                                                                   CredentialRequest credentialRequest,
                                                                                   String token) {
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
                                                                if (operationMode.equals(ASYNC)) {
                                                                    return deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
                                                                            .flatMap(credentialProcedureService::getSignerEmailFromDecodedCredentialByProcedureId)
                                                                            .flatMap(email ->
                                                                                    emailService.sendPendingCredentialNotification(email, "Pending Credential")
                                                                                            .then(Mono.just(credentialResponse))
                                                                            );
                                                                } else if (operationMode.equals(SYNC)) {
                                                                    return deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
                                                                            .flatMap(id -> credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(id)
                                                                                    .then(credentialProcedureService.getDecodedCredentialByProcedureId(id)
                                                                                            .flatMap(decodedCredential -> processDecodedCredential(processId, decodedCredential))
                                                                                    )
                                                                            )
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
        return verifiableCredentialService.generateDeferredCredentialResponse(processId, deferredCredentialRequest)
                .onErrorResume(e -> Mono.error(new RuntimeException("Failed to process the credential for the next processId: " + processId, e)));
    }

    @Override
    public Mono<Void> signDeferredCredential(String unsignedCredential, String userId, UUID credentialId, String token) {
        return null;
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


    private Mono<Void> processDecodedCredential(String processId, String decodedCredential) {
        log.info("ProcessID: {} Decoded Credential: {}", processId, decodedCredential);

        LEARCredentialEmployeeJwtPayload learCredentialEmployeeJwtPayload = credentialEmployeeFactory.mapStringToLEARCredentialEmployee(decodedCredential);

        String signerOrgIdentifier = learCredentialEmployeeJwtPayload.learCredentialEmployee().credentialSubject().mandate().signer().organizationIdentifier();
        if (signerOrgIdentifier == null || signerOrgIdentifier.isBlank()) {
            log.error("ProcessID: {} Signer Organization Identifier connot be null or empty", processId);
            return Mono.error(new IllegalArgumentException("Organization Identifier not valid"));
        }

        String mandatorOrgIdentifier = learCredentialEmployeeJwtPayload.learCredentialEmployee().credentialSubject().mandate().mandator().organizationIdentifier();
        if (mandatorOrgIdentifier == null || mandatorOrgIdentifier.isBlank()) {
            log.error("ProcessID: {} Mandator Organization Identifier connot be null or empty", processId);
            return Mono.error(new IllegalArgumentException("Organization Identifier not valid"));
        }

        return saveToTrustFramework(processId, signerOrgIdentifier, mandatorOrgIdentifier);
    }

    private Mono<Void> saveToTrustFramework(String processId, String signerOrgIdentifier, String mandatorOrgIdentifier) {

        String signerDid = DID_ELSI + signerOrgIdentifier;
        String mandatorDid = DID_ELSI + mandatorOrgIdentifier;

        return trustFrameworkService.validateDidFormat(processId, signerDid)
                .flatMap(isValid -> registerDidIfValid(processId, signerDid, isValid))
                .then(trustFrameworkService.validateDidFormat(processId, mandatorDid)
                        .flatMap(isValid -> registerDidIfValid(processId, mandatorDid, isValid)));
    }

    private Mono<Void> registerDidIfValid(String processId, String did, boolean isValid) {
        if (isValid) {
            return trustFrameworkService.registerDid(processId, did);
        } else {
            log.error("ProcessID: {} Did not registered because is invalid", processId);
            return Mono.empty();
        }
    }


}
