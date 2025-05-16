package es.in2.issuer.backend.shared.application.workflow.impl;

import com.nimbusds.jose.JWSObject;
import es.in2.issuer.backend.shared.application.workflow.CredentialIssuanceWorkflow;
import es.in2.issuer.backend.shared.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.backend.shared.domain.exception.*;
import es.in2.issuer.backend.shared.domain.model.dto.*;
import es.in2.issuer.backend.shared.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.backend.shared.domain.model.enums.CredentialStatus;
import es.in2.issuer.backend.shared.domain.service.*;
import es.in2.issuer.backend.shared.domain.util.factory.LEARCredentialEmployeeFactory;
import es.in2.issuer.backend.shared.infrastructure.config.AppConfig;
import es.in2.issuer.backend.shared.infrastructure.config.WebClientConfig;
import es.in2.issuer.backend.shared.infrastructure.config.security.service.VerifiableCredentialPolicyAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.naming.OperationNotSupportedException;
import java.text.ParseException;

import static es.in2.issuer.backend.backoffice.domain.util.Constants.*;
import static es.in2.issuer.backend.shared.domain.util.Constants.LEAR_CREDENTIAL_EMPLOYEE;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialIssuanceWorkflowImpl implements CredentialIssuanceWorkflow {

    private final AccessTokenService accessTokenService;
    private final VerifiableCredentialService verifiableCredentialService;
    private final AppConfig appConfig;
    private final ProofValidationService proofValidationService;
    private final EmailService emailService;
    private final CredentialProcedureService credentialProcedureService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;
    private final CredentialSignerWorkflow credentialSignerWorkflow;
    private final WebClientConfig webClient;
    private final VerifiableCredentialPolicyAuthorizationService verifiableCredentialPolicyAuthorizationService;
    private final TrustFrameworkService trustFrameworkService;
    private final LEARCredentialEmployeeFactory credentialEmployeeFactory;
    private final IssuerApiClientTokenService issuerApiClientTokenService;
    private final M2MTokenService m2mTokenService;

    @Override
    public Mono<Void> execute(String processId, PreSubmittedDataCredential preSubmittedDataCredential, String bearerToken, String idToken) {
        return accessTokenService.getCleanBearerToken(bearerToken).flatMap(
                token ->
                        verifiableCredentialPolicyAuthorizationService.authorize(token, preSubmittedDataCredential.schema(), preSubmittedDataCredential.payload(), idToken)
                                .then(Mono.defer(() -> {
                                    if (preSubmittedDataCredential.schema().equals(VERIFIABLE_CERTIFICATION)) {
                                        return issuanceFromServiceWithDelegatedAuthorization(processId, preSubmittedDataCredential, idToken);
                                    } else if (preSubmittedDataCredential.schema().equals(LEAR_CREDENTIAL_EMPLOYEE)) {
                                        return issuanceFromService(processId, preSubmittedDataCredential, token);
                                    }

                                    return Mono.error(new CredentialTypeUnsupportedException(preSubmittedDataCredential.schema()));
                                })));
    }

    private @NotNull Mono<Void> issuanceFromServiceWithDelegatedAuthorization(String processId, PreSubmittedDataCredential preSubmittedDataCredential, String idToken) {
        return ensurePreSubmittedCredentialResponseUriIsNotNullOrBlank(preSubmittedDataCredential)
                .then(ensureVerifiableCertificationHasIdToken(preSubmittedDataCredential, idToken)
                        .then(verifiableCredentialService.generateVerifiableCertification(processId, preSubmittedDataCredential, idToken)
                                .flatMap(procedureId -> issuerApiClientTokenService.getClientToken()
                                        .flatMap(internalToken -> credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(BEARER_PREFIX + internalToken, procedureId, JWT_VC))
                                        // TODO instead of updating the credential status to valid,
                                        //  we should update the credential status to pending download
                                        //  but we don't support the verifiable certification download yet
                                        .flatMap(encodedVc -> credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId)
                                                .then(m2mTokenService.getM2MToken()
                                                        .flatMap(m2mAccessToken ->
                                                                sendVcToResponseUri(
                                                                        preSubmittedDataCredential,
                                                                        encodedVc,
                                                                        m2mAccessToken.accessToken())))))));
    }

    private Mono<Void> ensurePreSubmittedCredentialResponseUriIsNotNullOrBlank(PreSubmittedDataCredential preSubmittedDataCredential) {
        if (preSubmittedDataCredential.responseUri() == null || preSubmittedDataCredential.responseUri().isBlank()) {
            return Mono.error(new OperationNotSupportedException("For schema: " + preSubmittedDataCredential.schema() + " response_uri is required"));
        }
        return Mono.empty();
    }

    private Mono<Void> ensureVerifiableCertificationHasIdToken(PreSubmittedDataCredential preSubmittedDataCredential, String idToken) {
        if (preSubmittedDataCredential.schema().equals(VERIFIABLE_CERTIFICATION) && idToken == null) {
            return Mono.error(new MissingIdTokenHeaderException("Missing required ID Token header for VerifiableCertification issuance."));
        }
        return Mono.empty();
    }

    private @NotNull Mono<Void> issuanceFromService(String processId, PreSubmittedDataCredential preSubmittedDataCredential, String token) {
        // todo: credentialIssuanceRecordService.create() ->
        //  --> buildCredentialIssuanceRecord [CredentialProcedure]
        //  -----> id -> random uuid
        //  -----> organizationIdentifier -> token
        // ------> credentialFormat -> preSubmittedCredentialRequest.format()
        //  -----> credentialType -> preSubmittedCredentialRequest.schema()
        //  -----> credentialData -> learCredentialEmployee.map(payload)
        //  -----> operationMode -> preSubmittedCredentialRequest.operationMode()
        //  -----> signatureMode -> hardcoded
        //  -----> createdAt
        //  -----> updatedAt
        //  --> SAVE
        // --> generateActivationCode (nonce) and save in cache (key: activationCode (nonce), value: credentialIssuanceRecordId)
        return verifiableCredentialService.generateVc(processId, preSubmittedDataCredential.schema(), preSubmittedDataCredential, token)
                .flatMap(transactionCode -> sendCredentialOfferEmail(transactionCode, preSubmittedDataCredential));
    }

    // todo: sendActivationCredentialEmail
    // todo: PreSubmittedCredentialRequest -> PreSubmittedDataCredential
    // todo: transactionCode -> activationCode
    private Mono<Void> sendCredentialOfferEmail(String transactionCode, PreSubmittedDataCredential preSubmittedDataCredential) {
        String email = preSubmittedDataCredential.payload().get(MANDATEE).get(EMAIL).asText();
        String user = preSubmittedDataCredential.payload().get(MANDATEE).get(FIRST_NAME).asText() + " " + preSubmittedDataCredential.payload().get(MANDATEE).get(LAST_NAME).asText();
        String organization = preSubmittedDataCredential.payload().get(MANDATOR).get(ORGANIZATION).asText();
        // todo: change to send to /activation-code
        // todo: path variable -> activation-code
        // todo: CHANGE IN FRONTEND
        // todo: change to https
        String credentialOfferUrl = UriComponentsBuilder
                .fromHttpUrl(appConfig.getIssuerFrontendUrl())
                .path("/credential-offer")
                .queryParam("transaction_code", transactionCode)
                .build()
                .toUriString();

        return emailService.sendCredentialActivationEmail(email, CREDENTIAL_ACTIVATION_EMAIL_SUBJECT, credentialOfferUrl, appConfig.getKnowledgebaseWalletUrl(), user, organization)
                .onErrorMap(exception ->
                        new EmailCommunicationException(MAIL_ERROR_COMMUNICATION_EXCEPTION_MESSAGE));
    }

    private Mono<Void> sendVcToResponseUri(PreSubmittedDataCredential preSubmittedDataCredential, String encodedVc, String token) {
        ResponseUriRequest responseUriRequest = ResponseUriRequest.builder()
                .encodedVc(encodedVc)
                .build();
        log.debug("Sending to response_uri: {} the VC: {} with the received token: {}", preSubmittedDataCredential.responseUri(), encodedVc, token);

        // Extract the product ID from the payload
        String productId = preSubmittedDataCredential.payload()
                .get(CREDENTIAL_SUBJECT)
                .get(PRODUCT)
                .get(PRODUCT_ID)
                .asText();
        // Extract the company email from the payload
        String companyEmail = preSubmittedDataCredential.payload()
                .get(CREDENTIAL_SUBJECT)
                .get(COMPANY)
                .get(EMAIL)
                .asText();

        return webClient.commonWebClient()
                .patch()
                .uri(preSubmittedDataCredential.responseUri())
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

            // TODO: rethink this logic instead of taking the first JWT proof
            return proofValidationService.isProofValid(credentialRequest.proofs().jwt().get(0), token)
                    .flatMap(isValid -> Boolean.TRUE.equals(isValid)
                            ? extractDidFromJwtProof(credentialRequest.proofs().jwt().get(0))
                            : Mono.error(new InvalidOrMissingProofException("Invalid proof")))
                    .flatMap(subjectDid -> deferredCredentialMetadataService.getOperationModeByAuthServerNonce(authServerNonce)
                            .flatMap(operationMode -> verifiableCredentialService.buildCredentialResponse(
                                            processId, subjectDid, authServerNonce, credentialRequest.format(), token)
                                    .flatMap(credentialResponse ->
                                            handleOperationMode(operationMode, processId, authServerNonce, credentialResponse)
                                    )
                            )
                    );
        } catch (ParseException e) {
            log.error("Error parsing the accessToken", e);
            throw new ParseErrorException("Error parsing accessToken");
        }
    }

    private Mono<VerifiableCredentialResponse> handleOperationMode(String operationMode, String processId, String authServerNonce, VerifiableCredentialResponse credentialResponse) {
        return switch (operationMode) {
            case ASYNC -> deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
                    .flatMap(credentialProcedureService::getSignerEmailFromDecodedCredentialByProcedureId)
                    .flatMap(email -> emailService.sendPendingCredentialNotification(email, "Pending Credential")
                            .thenReturn(credentialResponse));
            case SYNC -> deferredCredentialMetadataService.getProcedureIdByAuthServerNonce(authServerNonce)
                    .flatMap(id -> credentialProcedureService.getCredentialStatusByProcedureId(id)
                            .flatMap(status -> {
                                Mono<Void> updateMono = !CredentialStatus.PEND_SIGNATURE.toString().equals(status)
                                        ? credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(id)
                                        : Mono.empty();
                                return updateMono.then(credentialProcedureService.getDecodedCredentialByProcedureId(id));
                            })
                            .flatMap(decodedCredential -> processDecodedCredential(processId, decodedCredential))
                    )
                    .thenReturn(credentialResponse);
            default -> Mono.error(new IllegalArgumentException("Unknown operation mode: " + operationMode));
        };
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

        LEARCredentialEmployee learCredentialEmployee = credentialEmployeeFactory.mapStringToLEARCredentialEmployee(decodedCredential);

        String mandatorOrgIdentifier = learCredentialEmployee.credentialSubject().mandate().mandator().organizationIdentifier();
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
