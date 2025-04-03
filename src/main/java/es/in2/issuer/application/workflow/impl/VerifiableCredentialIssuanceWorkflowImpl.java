package es.in2.issuer.application.workflow.impl;

import com.nimbusds.jose.JWSObject;
import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.domain.exception.*;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.domain.model.enums.CredentialStatus;
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
import org.springframework.web.util.UriComponentsBuilder;
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
    @Override
    public Mono<Void> completeIssuanceCredentialProcess(String processId, String type, PreSubmittedCredentialRequest preSubmittedCredentialRequest, String token) {
        // Check if the format is not "json_vc_jwt"
        if (!JWT_VC_JSON.equals(preSubmittedCredentialRequest.format())) {
            return Mono.error(new FormatUnsupportedException("Format: " + preSubmittedCredentialRequest.format() + " is not supported"));
        }
        // Check if operation_mode is different to sync
        if (!preSubmittedCredentialRequest.operationMode().equals(SYNC)) {
            return Mono.error(new OperationNotSupportedException("operation_mode: " + preSubmittedCredentialRequest.operationMode() + " with schema: " + preSubmittedCredentialRequest.schema()));
        }

        // Validate user policy before proceeding
        return policyAuthorizationService.authorize(token, preSubmittedCredentialRequest.schema(), preSubmittedCredentialRequest.payload())
                .then(Mono.defer(() -> {
                    if (preSubmittedCredentialRequest.schema().equals(LEAR_CREDENTIAL_EMPLOYEE)) {
                        return verifiableCredentialService.generateVc(processId, type, preSubmittedCredentialRequest, token)
                                .flatMap(transactionCode -> sendCredentialOfferEmail(transactionCode, preSubmittedCredentialRequest));
                    } else if (preSubmittedCredentialRequest.schema().equals(VERIFIABLE_CERTIFICATION)) {
                        // Check if responseUri is null, empty, or only contains whitespace
                        if (preSubmittedCredentialRequest.responseUri() == null || preSubmittedCredentialRequest.responseUri().isBlank()) {
                            return Mono.error(new OperationNotSupportedException("For schema: " + preSubmittedCredentialRequest.schema() + " response_uri is required"));
                        }
                        return verifiableCredentialService.generateVerifiableCertification(processId, type, preSubmittedCredentialRequest, token)
                                .flatMap(procedureId -> issuerApiClientTokenService.getClientToken()
                                    .flatMap(internalToken -> credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(BEARER_PREFIX + internalToken, procedureId, JWT_VC))
                                        // todo instead of updating the credential status to valid, we should update the credential status to pending download but we don't support the verifiable certification download yet
                                        .flatMap(encodedVc -> credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId)
                                        .then(sendVcToResponseUri(preSubmittedCredentialRequest, encodedVc, token))
                                ));
                    }
                    return Mono.error(new CredentialTypeUnsupportedException(type));
                }));
    }

    private Mono<Void> sendCredentialOfferEmail(String transactionCode, PreSubmittedCredentialRequest preSubmittedCredentialRequest){
        String email = preSubmittedCredentialRequest.payload().get(MANDATEE).get(EMAIL).asText();
        String user = preSubmittedCredentialRequest.payload().get(MANDATEE).get(FIRST_NAME).asText() + " " + preSubmittedCredentialRequest.payload().get(MANDATEE).get(LAST_NAME).asText();
        String organization = preSubmittedCredentialRequest.payload().get(MANDATOR).get(ORGANIZATION).asText();
        String credentialOfferUrl = UriComponentsBuilder
                .fromHttpUrl(appConfig.getIssuerUiExternalDomain())
                .path("/credential-offer")
                .queryParam("transaction_code", transactionCode)
                .build()
                .toUriString();

        return emailService.sendCredentialActivationEmail(email, SEND_CREDENTIAL_ACTIVATION_EMAIL_SUBJECT, credentialOfferUrl, appConfig.getKnowledgebaseWalletUrl(), user, organization)
                .onErrorMap(e ->
                        new EmailCommunicationException(MAIL_ERROR_COMMUNICATION_EXCEPTION));
    }

    private Mono<Void> sendVcToResponseUri(PreSubmittedCredentialRequest preSubmittedCredentialRequest, String encodedVc, String token) {
        ResponseUriRequest responseUriRequest = ResponseUriRequest.builder()
                .encodedVc(encodedVc)
                .build();
        log.debug("Sending to response_uri: {} the VC: {} with the received token: {}", preSubmittedCredentialRequest.responseUri(), encodedVc, token);

        // Extract the product ID from the payload
        String productId = preSubmittedCredentialRequest.payload()
                .get(CREDENTIAL_SUBJECT)
                .get(PRODUCT)
                .get(PRODUCT_ID)
                .asText();
        // Extract the company email from the payload
        String companyEmail = preSubmittedCredentialRequest.payload()
                .get(CREDENTIAL_SUBJECT)
                .get(COMPANY)
                .get(EMAIL)
                .asText();

        return webClient.commonWebClient()
                .patch()
                .uri(preSubmittedCredentialRequest.responseUri())
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
                    .flatMap(isValid -> Boolean.TRUE.equals(isValid)
                            ? extractDidFromJwtProof(credentialRequest.proof().jwt())
                            : Mono.error(new InvalidOrMissingProofException("Invalid proof")))
                    .flatMap(subjectDid ->
                            deferredCredentialMetadataService.getOperationModeByAuthServerNonce(authServerNonce)
                                    .flatMap(operationMode ->
                                            verifiableCredentialService.buildCredentialResponse(
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
