package es.in2.issuer.application.workflow.impl;

import com.nimbusds.jose.JWSObject;
import com.upokecenter.cbor.CBORObject;
import es.in2.issuer.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.domain.exception.Base45Exception;
import es.in2.issuer.domain.exception.CredentialTypeUnsupportedException;
import es.in2.issuer.domain.exception.InvalidOrMissingProofException;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.model.enums.SignatureType;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.infrastructure.config.AppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.minvws.encoding.Base45;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.text.ParseException;
import java.util.Base64;
import java.util.Collections;
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

    @Override
    public Mono<Void> completeWithdrawLearCredentialProcess(String processId, String type, CredentialData credentialData) {
        return verifiableCredentialService.generateVc(processId, type, credentialData)
                .flatMap(transactionCode -> {
                    String email;
                    String name;
                    if (LEAR_CREDENTIAL_EMPLOYEE.equals(type)) {
                        email = credentialData.credential().get("mandatee").get("email").asText();
                        name = credentialData.credential().get("mandatee").get("first_name").asText();
                    } else if (VERIFIABLE_CERTIFICATION.equals(type)) {
                        email = credentialData.credential().get("credentialSubject").get("company").get("email").asText();
                        name = credentialData.credential().get("credentialSubject").get("company").get("commonName").asText();
                    } else {
                        return Mono.error(new CredentialTypeUnsupportedException(type));
                    }
                    return emailService.sendTransactionCodeForCredentialOffer(email, "Credential Offer", appConfig.getIssuerUiExternalDomain() + "/credential-offer?transaction_code=" + transactionCode, name, appConfig.getWalletUrl());
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
            throw new RuntimeException("Error parsing accessToken", e);
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

    @Override
    public Mono<String> signCredentialOnRequestedFormat(String unsignedCredential, String format, String token) {
        return Mono.defer(() -> {
            if (format.equals(JWT_VC)) {
                log.info(unsignedCredential);
                log.info("Signing credential in JADES remotely ...");
                SignatureRequest signatureRequest = new SignatureRequest(
                        new SignatureConfiguration(SignatureType.JADES, Collections.emptyMap()),
                        unsignedCredential
                );
                return remoteSignatureService.sign(signatureRequest, token)
                        .publishOn(Schedulers.boundedElastic())
                        .map(SignedData::data);
            } else if (format.equals(CWT_VC)) {
                log.info(unsignedCredential);
                return generateCborFromJson(unsignedCredential)
                        .flatMap(cbor -> generateCOSEBytesFromCBOR(cbor, token))
                        .flatMap(this::compressAndConvertToBase45FromCOSE);
            } else {
                return Mono.error(new IllegalArgumentException("Unsupported credential format: " + format));
            }
        });
    }

    /**
     * Generate CBOR payload for COSE.
     *
     * @param edgcJson EDGC payload as JSON string
     * @return Mono emitting CBOR bytes
     */
    private Mono<byte[]> generateCborFromJson(String edgcJson) {
        return Mono.fromCallable(() -> CBORObject.FromJSONString(edgcJson).EncodeToBytes());
    }

    /**
     * Generate COSE bytes from CBOR bytes.
     *
     * @param cbor  CBOR bytes
     * @param token Authentication token
     * @return Mono emitting COSE bytes
     */
    private Mono<byte[]> generateCOSEBytesFromCBOR(byte[] cbor, String token) {
        log.info("Signing credential in COSE format remotely ...");
        String cborBase64 = Base64.getEncoder().encodeToString(cbor);
        SignatureRequest signatureRequest = new SignatureRequest(
                new SignatureConfiguration(SignatureType.COSE, Collections.emptyMap()),
                cborBase64
        );
        return remoteSignatureService.sign(signatureRequest, token).map(signedData -> Base64.getDecoder().decode(signedData.data()));
    }

    /**
     * Compress COSE bytes and convert it to Base45.
     *
     * @param cose COSE Bytes
     * @return Mono emitting COSE bytes compressed and in Base45
     */
    private Mono<String> compressAndConvertToBase45FromCOSE(byte[] cose) {
        return Mono.fromCallable(() -> {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try (CompressorOutputStream deflateOut = new CompressorStreamFactory()
                    .createCompressorOutputStream(CompressorStreamFactory.DEFLATE, stream)) {
                deflateOut.write(cose);
            } // Automatically closed by try-with-resources
            byte[] zip = stream.toByteArray();
            return Base45.getEncoder().encodeToString(zip);
        }).onErrorResume(e -> {
            log.error("Error compressing and converting to Base45: " + e.getMessage(), e);
            return Mono.error(new Base45Exception("Error compressing and converting to Base45"));
        });
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
