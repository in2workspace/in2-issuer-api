package es.in2.issuer.application.workflow.impl;

import com.nimbusds.jose.JWSObject;
import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.domain.exception.*;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.domain.util.factory.LEARCredentialEmployeeFactory;
import es.in2.issuer.infrastructure.config.AppConfig;
import es.in2.issuer.infrastructure.config.WebClientConfig;
import es.in2.issuer.infrastructure.config.security.service.PolicyAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.naming.OperationNotSupportedException;
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
    private final PolicyAuthorizationService policyAuthorizationService;
    private final TrustFrameworkService trustFrameworkService;
    private final LEARCredentialEmployeeFactory credentialEmployeeFactory;
    private final IssuerApiClientTokenService issuerApiClientTokenService;
    private final M2MTokenService m2MTokenService;

    @Override
    public Mono<Void> completeIssuanceCredentialProcess(String processId, IssuanceRequest issuanceRequest, String token, String idToken) {
        // Check if the format is not "json_vc_jwt"
        if (!JWT_VC_JSON.equals(issuanceRequest.format())) {
            return Mono.error(new FormatUnsupportedException("Format: " + issuanceRequest.format() + " is not supported"));
        }
        // Check if operation_mode is different to sync
        if (!issuanceRequest.operationMode().equals(SYNC)) {
            return Mono.error(new OperationNotSupportedException("operation_mode: " + issuanceRequest.operationMode() + " with schema: " + issuanceRequest.schema()));
        }

        // Validate user policy before proceeding
        return policyAuthorizationService.authorize(token, issuanceRequest.schema(), issuanceRequest.payload())
                .then(Mono.defer(() -> {
                    if (issuanceRequest.schema().equals(LEAR_CREDENTIAL_EMPLOYEE)) {
                        return verifiableCredentialService.generateVc(processId, issuanceRequest.schema(), issuanceRequest, token)
                                .flatMap(transactionCode -> sendCredentialOfferEmail(transactionCode, issuanceRequest));
                    } else if (issuanceRequest.schema().equals(VERIFIABLE_CERTIFICATION)) {
                        // Validate idToken header for VerifiableCertification schema
                        if (idToken == null || idToken.isBlank()) {
                            return Mono.error(new MissingIdTokenHeaderException("Missing required ID Token header for VerifiableCertification issuance."));
                        }
                        // Check if responseUri is null, empty, or only contains whitespace
                        if (issuanceRequest.responseUri() == null || issuanceRequest.responseUri().isBlank()) {
                            return Mono.error(new OperationNotSupportedException("For schema: " + issuanceRequest.schema() + " response_uri is required"));
                        }
                        return verifiableCredentialService.generateVerifiableCertification(processId, issuanceRequest, idToken)
                                .flatMap(procedureId -> issuerApiClientTokenService.getClientToken()
                                        .flatMap(internalToken -> credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(BEARER_PREFIX + internalToken, procedureId, JWT_VC))
                                        // todo instead of updating the credential status to valid, we should update the credential status to pending download but we don't support the verifiable certification download yet
                                        .flatMap(encodedVc -> credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId)
                                                .then(m2MTokenService.getM2MToken()
                                                        .flatMap(m2mAccessToken ->
                                                                sendVcToResponseUri(
                                                                        issuanceRequest,
                                                                        encodedVc,
                                                                        m2mAccessToken.accessToken())))));
                    }
                    return Mono.error(new CredentialTypeUnsupportedException(issuanceRequest.schema()));
                }));
    }

    private Mono<Void> sendCredentialOfferEmail(String transactionCode, IssuanceRequest issuanceRequest) {
        String email = issuanceRequest.payload().get(MANDATEE).get(EMAIL).asText();
        String user = issuanceRequest.payload().get(MANDATEE).get(FIRST_NAME).asText() + " " + issuanceRequest.payload().get(MANDATEE).get(LAST_NAME).asText();
        String organization = issuanceRequest.payload().get(MANDATOR).get(ORGANIZATION).asText();
        return emailService.sendTransactionCodeForCredentialOffer(email, "Activate your new credential", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, appConfig.getKnowledgebaseWalletUrl(), user, organization);
    }

    private Mono<Void> sendVcToResponseUri(IssuanceRequest issuanceRequest, String encodedVc, String token) {
        ResponseUriRequest responseUriRequest = ResponseUriRequest.builder()
                .encodedVc(encodedVc)
                .build();
        log.info("Sending to response_uri: {} the VC: {} with the received token: {}", issuanceRequest.responseUri(), encodedVc, token);

        // Extract the product ID from the payload
        String productId = issuanceRequest.payload()
                .get("credentialSubject")
                .get("product")
                .get("productId")
                .asText();
        // Extract the company email from the payload
        String companyEmail = issuanceRequest.payload()
                .get("credentialSubject")
                .get("company")
                .get("email")
                .asText();

        return webClient.commonWebClient()
                .patch()
                .uri(issuanceRequest.responseUri())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                .bodyValue(responseUriRequest)
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        if (HttpStatus.ACCEPTED.equals(response.statusCode())) {
                            log.info("Received 202 from response_uri. Extracting HTML and sending specific mail for missing documents");
                            // Retrieve the HTML body from the response
                            return response.bodyToMono(String.class)
                                    .flatMap(htmlResponseBody -> emailService.sendResponseUriAcceptedWithHtml(companyEmail, productId, htmlResponseBody))
                                    .then();
                        }
                        return Mono.empty();
                    } else {
                        log.error("Non-2xx status code received: {}. Sending failure email...", response.statusCode());
                        return emailService.sendResponseUriFailed(companyEmail, productId, appConfig.getKnowledgeBaseUploadCertificationGuideUrl())
                                .then();
                    }
                })
                .onErrorResume(WebClientRequestException.class, ex -> {
                    log.error("Network error while sending VC to response_uri", ex);
                    return emailService.sendResponseUriFailed(companyEmail, productId, appConfig.getKnowledgeBaseUploadCertificationGuideUrl())
                            .then();
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

        LEARCredentialEmployeeJwtPayload learCredentialEmployeeJwtPayload = credentialEmployeeFactory.mapStringToLEARCredentialEmployeeJwtPayload(decodedCredential);

        String mandatorOrgIdentifier = learCredentialEmployeeJwtPayload.learCredentialEmployee().credentialSubject().mandate().mandator().organizationIdentifier();
        if (mandatorOrgIdentifier == null || mandatorOrgIdentifier.isBlank()) {
            log.error("ProcessID: {} Mandator Organization Identifier cannot be null or empty", processId);
            return Mono.error(new IllegalArgumentException("Organization Identifier not valid"));
        }

        return saveToTrustFramework(processId, mandatorOrgIdentifier);
    }

    private Mono<Void> saveToTrustFramework(String processId, String mandatorOrgIdentifier) {

        String mandatorDid = DID_ELSI + mandatorOrgIdentifier;

        return trustFrameworkService.validateDidFormat(processId, mandatorDid)
                .flatMap(isValid -> registerDidIfValid(processId, mandatorDid, isValid));
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
