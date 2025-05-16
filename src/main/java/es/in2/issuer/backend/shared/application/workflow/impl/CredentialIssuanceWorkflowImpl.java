package es.in2.issuer.backend.shared.application.workflow.impl;

import com.nimbusds.jose.JWSObject;
import es.in2.issuer.backend.shared.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.backend.shared.application.workflow.CredentialIssuanceWorkflow;
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
import static es.in2.issuer.backend.shared.domain.util.Constants.JWT_VC_JSON;
import static es.in2.issuer.backend.shared.domain.util.Constants.LEAR_CREDENTIAL_EMPLOYEE;

@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialIssuanceWorkflowImpl implements CredentialIssuanceWorkflow {

    private final CredentialDeliveryService credentialDeliveryService;
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
    public Mono<Void> execute(String processId, PreSubmittedCredentialRequest preSubmittedCredentialRequest, String token, String idToken) {

        // Check if the format is not "json_vc_jwt"
        if (!JWT_VC_JSON.equals(preSubmittedCredentialRequest.format())) {
            return Mono.error(new FormatUnsupportedException("Format: " + preSubmittedCredentialRequest.format() + " is not supported"));
        }
        // Check if operation_mode is different to sync
        if (!preSubmittedCredentialRequest.operationMode().equals(SYNC)) {
            return Mono.error(new OperationNotSupportedException("operation_mode: " + preSubmittedCredentialRequest.operationMode() + " with schema: " + preSubmittedCredentialRequest.schema()));
        }

        // Validate idToken header for VerifiableCertification schema
        if (preSubmittedCredentialRequest.schema().equals(VERIFIABLE_CERTIFICATION) && idToken == null) {
            return Mono.error(new MissingIdTokenHeaderException("Missing required ID Token header for VerifiableCertification issuance."));
        }

        // Validate user policy before proceeding
        return verifiableCredentialPolicyAuthorizationService.authorize(token, preSubmittedCredentialRequest.schema(), preSubmittedCredentialRequest.payload(), idToken)
                .then(Mono.defer(() -> {
                    if (preSubmittedCredentialRequest.schema().equals(LEAR_CREDENTIAL_EMPLOYEE)) {
                        return verifiableCredentialService.generateVc(processId, preSubmittedCredentialRequest.schema(), preSubmittedCredentialRequest, token)
                                .flatMap(transactionCode -> sendCredentialOfferEmail(transactionCode, preSubmittedCredentialRequest));
                    } else if (preSubmittedCredentialRequest.schema().equals(VERIFIABLE_CERTIFICATION)) {
                        // Check if responseUri is null, empty, or only contains whitespace
                        // todo
                        log.info(preSubmittedCredentialRequest.toString());
                        log.info("Valor de responseUri rebut: '{}'", preSubmittedCredentialRequest.responseUri());
                        if (preSubmittedCredentialRequest.responseUri() == null || preSubmittedCredentialRequest.responseUri().isBlank()) {
                            return Mono.error(new OperationNotSupportedException("For schema: " + preSubmittedCredentialRequest.schema() + " response_uri is required"));
                        }
                        return verifiableCredentialService.generateVerifiableCertification(processId, preSubmittedCredentialRequest, idToken)
                                .flatMap(procedureId -> issuerApiClientTokenService.getClientToken()
                                        .flatMap(internalToken -> credentialSignerWorkflow.signAndUpdateCredentialByProcedureId(BEARER_PREFIX + internalToken, procedureId, JWT_VC))
                                        .flatMap(encodedVc ->
                                                credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId)
                                                        .then(m2mTokenService.getM2MToken()
                                                                .flatMap(m2mAccessToken -> {
                                                                    String responseUri = preSubmittedCredentialRequest.responseUri();
                                                                    String productId = preSubmittedCredentialRequest.payload()
                                                                            .get(CREDENTIAL_SUBJECT)
                                                                            .get(PRODUCT)
                                                                            .get(PRODUCT_ID)
                                                                            .asText();
                                                                    String companyEmail = preSubmittedCredentialRequest.payload()
                                                                            .get(CREDENTIAL_SUBJECT)
                                                                            .get(COMPANY)
                                                                            .get(EMAIL)
                                                                            .asText();

                                                                    return credentialDeliveryService.sendVcToResponseUri(
                                                                            responseUri,
                                                                            encodedVc,
                                                                            m2mAccessToken.accessToken(),
                                                                            productId,
                                                                            companyEmail
                                                                    );
                                                                })
                                                        )
                                        )
                                );
                    }
                    return Mono.error(new CredentialTypeUnsupportedException(preSubmittedCredentialRequest.schema()));
                }));
    }

    private Mono<Void> sendCredentialOfferEmail(String transactionCode, PreSubmittedCredentialRequest preSubmittedCredentialRequest) {
        String email = preSubmittedCredentialRequest.payload().get(MANDATEE).get(EMAIL).asText();
        String user = preSubmittedCredentialRequest.payload().get(MANDATEE).get(FIRST_NAME).asText() + " " + preSubmittedCredentialRequest.payload().get(MANDATEE).get(LAST_NAME).asText();
        String organization = preSubmittedCredentialRequest.payload().get(MANDATOR).get(ORGANIZATION).asText();
        // TODO we are only validating that the url its well formed, we should return the proper object not a string
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
