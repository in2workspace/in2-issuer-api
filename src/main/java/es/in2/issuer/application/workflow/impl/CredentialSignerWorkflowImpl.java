package es.in2.issuer.application.workflow.impl;

import com.upokecenter.cbor.CBORObject;
import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.application.workflow.DeferredCredentialWorkflow;
import es.in2.issuer.domain.exception.Base45Exception;
import es.in2.issuer.domain.model.dto.*;
import es.in2.issuer.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.domain.model.enums.SignatureType;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.service.RemoteSignatureService;
import es.in2.issuer.domain.util.factory.LEARCredentialEmployeeFactory;
import es.in2.issuer.domain.util.factory.VerifiableCertificationFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.minvws.encoding.Base45;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import static es.in2.issuer.domain.util.Constants.CWT_VC;
import static es.in2.issuer.domain.util.Constants.JWT_VC;

@Service
@Slf4j
@RequiredArgsConstructor
public class CredentialSignerWorkflowImpl implements CredentialSignerWorkflow {

    private final DeferredCredentialWorkflow deferredCredentialWorkflow;
    private final CredentialProcedureService credentialProcedureService;
    private final RemoteSignatureService remoteSignatureService;
    private final LEARCredentialEmployeeFactory learCredentialEmployeeFactory;
    private final VerifiableCertificationFactory verifiableCertificationFactory;

    @Override
    public Mono<String> signAndUpdateCredentialByProcedureId(String authorizationHeader, String procedureId, String format) {
        return credentialProcedureService.getDecodedCredentialByProcedureId(procedureId)
                .flatMap(decodedCredential -> {
                    try{
                        if(decodedCredential.contains("vc")){
                            log.info("JWT Payload already created");
                            return signCredentialOnRequestedFormat(decodedCredential, format, authorizationHeader, procedureId);
                        }
                        log.info("Building JWT payload for credential signing.");
                        if(decodedCredential.contains("VerifiableCertification")){
                            VerifiableCertification verifiableCertification = verifiableCertificationFactory.mapStringToVerifiableCertification(decodedCredential);
                            return verifiableCertificationFactory.buildVerifiableCertificationJwtPayload(verifiableCertification)
                                    .flatMap(verifiableCertificationFactory::convertVerifiableCertificationJwtPayloadInToString)
                                    .flatMap(unsignedCredential -> {
                                        log.info("Start get signed credential.");
                                        return signCredentialOnRequestedFormat(unsignedCredential, format, authorizationHeader, procedureId);
                                    });

                        } else {
                            LEARCredentialEmployee learCredentialEmployee = learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(decodedCredential);
                            return learCredentialEmployeeFactory.buildLEARCredentialEmployeeJwtPayload(learCredentialEmployee)
                                    .flatMap(learCredentialEmployeeFactory::convertLEARCredentialEmployeeJwtPayloadInToString)
                                    .flatMap(unsignedCredential -> {
                                        log.info("Start get signed credential.");
                                        return signCredentialOnRequestedFormat(unsignedCredential, format, authorizationHeader, procedureId);
                                    });
                        }
                    }
                    catch (Exception e){
                        log.error("Error signing credential: " + e.getMessage(), e);
                        return Mono.error(new IllegalArgumentException("Error signing credential"));
                    }
                })
                .flatMap(signedCredential -> {
                    log.info("Update Signed Credential");
                    return updateSignedCredential(signedCredential)
                            .thenReturn(signedCredential);
                })
                .doOnSuccess(x -> log.info("Credential Signed and updated successfully."));
    }

    private Mono<Void> updateSignedCredential(String signedCredential) {
        List<SignedCredentials.SignedCredential> credentials = List.of(SignedCredentials.SignedCredential.builder().credential(signedCredential).build());
        SignedCredentials signedCredentials = new SignedCredentials(credentials);
        return deferredCredentialWorkflow.updateSignedCredentials(signedCredentials);
    }

    private Mono<String> signCredentialOnRequestedFormat(String unsignedCredential, String format, String token, String procedureId) {
        return Mono.defer(() -> {
            if (format.equals(JWT_VC)) {
                log.info("Credential Payload {}", unsignedCredential);
                log.info("Signing credential in JADES remotely ...");
                SignatureRequest signatureRequest = new SignatureRequest(
                        new SignatureConfiguration(SignatureType.JADES, Collections.emptyMap()),
                        unsignedCredential
                );

                return remoteSignatureService.sign(signatureRequest, token, procedureId)
                        .doOnSubscribe(s -> {})
                        .doOnNext(data -> {})
                        .publishOn(Schedulers.boundedElastic())
                        .map(SignedData::data)
                        .doOnSuccess(result -> {})
                        .doOnError(e -> {});
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
        return remoteSignatureService.sign(signatureRequest, token, "").map(signedData -> Base64.getDecoder().decode(signedData.data()));
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
}
