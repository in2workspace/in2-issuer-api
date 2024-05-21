package es.in2.issuer.application.service.impl;

import com.nimbusds.jose.JWSObject;
import com.upokecenter.cbor.CBORObject;
import es.in2.issuer.application.service.VerifiableCredentialIssuanceService;
import es.in2.issuer.domain.entity.CredentialProcedure;
import es.in2.issuer.domain.exception.Base45Exception;
import es.in2.issuer.domain.exception.InvalidOrMissingProofException;
import es.in2.issuer.domain.exception.TemplateReadException;
import es.in2.issuer.domain.exception.UserDoesNotExistException;
import es.in2.issuer.domain.model.*;
import es.in2.issuer.domain.service.*;
import es.in2.issuer.infrastructure.config.AppConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.minvws.encoding.Base45;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.*;
import static es.in2.issuer.domain.util.Utils.generateCustomNonce;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerifiableCredentialIssuanceServiceImpl implements VerifiableCredentialIssuanceService {

    // fixme: this is a temporary solution to load credential templates from resources
    @Value("classpath:credentials/templates/LEARCredentialEmployee.json")
    private Resource learCredentialTemplate;

    private final RemoteSignatureService remoteSignatureService;
    private final AuthenticSourcesRemoteService authenticSourcesRemoteService;
    private final VerifiableCredentialService verifiableCredentialService;
    private final AppConfiguration appConfiguration;
    private final ProofValidationService proofValidationService;
    private final CredentialManagementService credentialManagementService;
    private final EmailService emailService;

    @Override
    public Mono<Void> completeWithdrawLearCredentialProcess(String processId, LEARCredentialRequest learCredentialRequest) {
        return verifiableCredentialService.generateVc(processId,LEAR_CREDENTIAL_EMPLOYEE,learCredentialRequest)
                .flatMap(transactionCode -> emailService());

    }
    @Override
    public Mono<VerifiableCredentialResponse> generateVerifiableCredentialResponse(
            String userId,
            CredentialRequest credentialRequest,
            String token
    ) {
        return proofValidationService.isProofValid(credentialRequest.proof().jwt())
                .flatMap(isValid -> {
                    if (Boolean.FALSE.equals(isValid)) {
                        return Mono.error(new InvalidOrMissingProofException("Invalid proof"));
                    }
                    return getNonceClaim(credentialRequest.proof().jwt());
                })
                .flatMap(nonceClaim -> extractDidFromJwtProof(credentialRequest.proof().jwt())
                        .flatMap(subjectDid -> {
                            String format = credentialRequest.format();
                            return generateUnsignedVerifiableCredential(subjectDid)
                                    .flatMap(credential -> credentialManagementService.commitCredential(credential, userId, format)
                                        .flatMap(transactionId -> Mono.just(new VerifiableCredentialResponse(credential, transactionId, nonceClaim, 600))));
                        }));
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
    public Mono<VerifiableCredentialResponse> generateVerifiableCredentialDeferredResponse(String userId, DeferredCredentialRequest deferredCredentialRequest, String token){
        return credentialManagementService.getDeferredCredentialByTransactionId(deferredCredentialRequest.transactionId())
                .flatMap(deferredCredential -> {
                    if (deferredCredential.getCredentialSigned() == null || deferredCredential.getCredentialSigned().isBlank()) {
                        return credentialManagementService.updateTransactionId(deferredCredentialRequest.transactionId())
                                .map(newTransactionId -> VerifiableCredentialResponse.builder()
                                        .transactionId(newTransactionId)
                                        .build());
                    } else {
                        return credentialManagementService.deleteCredentialDeferred(deferredCredentialRequest.transactionId())
                                .then(Mono.just(VerifiableCredentialResponse.builder()
                                        .credential(deferredCredential.getCredentialSigned())
                                        .build()));
                    }
                })
                .onErrorResume(e -> Mono.error(new RuntimeException("Failed to process the credential.", e)));
    }

    @Override
    public Mono<Void> signDeferredCredential(String unsignedCredential, String userId, UUID credentialId, String token){
            return verifiableCredentialService.generateDeferredVcPayLoad(unsignedCredential)
                    .flatMap(vcPayload -> {
                        SignatureRequest signatureRequest = SignatureRequest.builder()
                                .configuration(SignatureConfiguration.builder().type(SignatureType.JADES).parameters(Collections.emptyMap()).build())
                                .data(vcPayload)
                                .build();
                        return remoteSignatureService.sign(signatureRequest, token)
                                .publishOn(Schedulers.boundedElastic())
                                .map(SignedData::data);
                    })
                    .flatMap(signedCredential -> credentialManagementService.updateCredential(signedCredential, credentialId, userId))
                    .onErrorResume(e -> Mono.error(new RuntimeException("Failed to sign and update the credential.", e)));
    }

    @Override
    public Mono<String> signCredentialOnRequestedFormat(String unsignedCredential, String format, String userId, UUID credentialId, String token) {
        return Mono.defer(() -> {
        if(format.equals(JWT_VC)){
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
        }});
    }

    private Mono<String> generateUnsignedVerifiableCredential(String subjectDid) {
        return Mono.defer(() -> {
            try {
                Instant expiration = Instant.now().plus(30, ChronoUnit.DAYS);
                // Prepare desired custom data that should replace the default template data
                log.info("Fetching information from authentic sources ...");
                //return authenticSourcesRemoteService.getUser(token)
                return authenticSourcesRemoteService.getUserFromLocalFile()
                        .flatMap(userData -> {
                            // todo get issuer did from dss module
                            // Get Credential template from local file
                            String learTemplate;
                            try {
                                learTemplate = new String(learCredentialTemplate.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                            } catch (IOException e) {
                                return Mono.error(new TemplateReadException("Error when reading template"));
                            }

                            return verifiableCredentialService.bindTheUserDidToHisCredential(learTemplate, subjectDid, appConfiguration.getIssuerDid(), userData, expiration);
                        });
            } catch (UserDoesNotExistException e) {
                log.error("UserDoesNotExistException {}", e.getMessage());
                return Mono.error(new UserDoesNotExistException("User Does Not Exist"));
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

    private Mono<String> getNonceClaim(String jwtProof) {
        return Mono.fromCallable(() -> {
            JWSObject jwsObject = JWSObject.parse(jwtProof);
            return jwsObject.getPayload().toJSONObject().get("nonce").toString();
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
