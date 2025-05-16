package es.in2.issuer.backend.shared.application.workflow.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.upokecenter.cbor.CBORObject;
import es.in2.issuer.backend.shared.application.workflow.CredentialSignerWorkflow;
import es.in2.issuer.backend.shared.application.workflow.DeferredCredentialWorkflow;
import es.in2.issuer.backend.shared.domain.exception.Base45Exception;
import es.in2.issuer.backend.shared.domain.model.dto.*;
import es.in2.issuer.backend.shared.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.backend.shared.domain.model.enums.SignatureType;
import es.in2.issuer.backend.shared.domain.service.*;
import es.in2.issuer.backend.shared.domain.util.factory.LEARCredentialEmployeeFactory;
import es.in2.issuer.backend.shared.domain.util.factory.VerifiableCertificationFactory;
import es.in2.issuer.backend.shared.infrastructure.repository.CredentialProcedureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.minvws.encoding.Base45;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.naming.OperationNotSupportedException;
import java.io.ByteArrayOutputStream;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static es.in2.issuer.backend.backoffice.domain.util.Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CredentialSignerWorkflowImpl implements CredentialSignerWorkflow {

    private final DeferredCredentialWorkflow deferredCredentialWorkflow;
    private final RemoteSignatureService remoteSignatureService;
    private final LEARCredentialEmployeeFactory learCredentialEmployeeFactory;
    private final VerifiableCertificationFactory verifiableCertificationFactory;
    private final CredentialProcedureRepository credentialProcedureRepository;
    private final CredentialProcedureService credentialProcedureService;
    private final M2MTokenService m2mTokenService;
    private final CredentialDeliveryService credentialDeliveryService;
    private final DeferredCredentialMetadataService deferredCredentialMetadataService;

    @Override
    public Mono<String> signAndUpdateCredentialByProcedureId(String authorizationHeader, String procedureId, String format) {
        log.info("signAndUpdateCredentialByProcedureId");
        return credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId))
            .flatMap(credentialProcedure -> {
                try{
                    //TODO eliminar este if en Junio aprox cuando ya no queden credenciales sin vc sin firmar
                    if(credentialProcedure.getCredentialDecoded().contains("\"vc\"")){
                        log.info("JWT Payload already created");
                        return signCredentialOnRequestedFormat(credentialProcedure.getCredentialDecoded(), format, authorizationHeader, procedureId);
                    }
                    String credentialType = credentialProcedure.getCredentialType();
                    log.info("Building JWT payload for credential signing for credential with type: {}", credentialType);
                    return switch (credentialType) {
                        case "VERIFIABLE_CERTIFICATION" -> {
                            VerifiableCertification verifiableCertification = verifiableCertificationFactory
                                    .mapStringToVerifiableCertification(credentialProcedure.getCredentialDecoded());
                            yield verifiableCertificationFactory.buildVerifiableCertificationJwtPayload(verifiableCertification)
                                    .flatMap(verifiableCertificationFactory::convertVerifiableCertificationJwtPayloadInToString)
                                    .flatMap(unsignedCredential -> signCredentialOnRequestedFormat(unsignedCredential, format, authorizationHeader, procedureId));
                        }
                        case "LEAR_CREDENTIAL_EMPLOYEE" -> {
                            LEARCredentialEmployee learCredentialEmployee = learCredentialEmployeeFactory
                                    .mapStringToLEARCredentialEmployee(credentialProcedure.getCredentialDecoded());
                            yield learCredentialEmployeeFactory.buildLEARCredentialEmployeeJwtPayload(learCredentialEmployee)
                                    .flatMap(learCredentialEmployeeFactory::convertLEARCredentialEmployeeJwtPayloadInToString)
                                    .flatMap(unsignedCredential -> signCredentialOnRequestedFormat(unsignedCredential, format, authorizationHeader, procedureId));
                        }
                        default -> {
                            log.error("Unsupported credential type: {}", credentialType);
                            yield Mono.error(new IllegalArgumentException("Unsupported credential type: " + credentialType));
                        }
                    };
                }
                catch (Exception e){
                    log.error("Error signing credential with procedure id: {} - {}", procedureId, e.getMessage(), e);
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
                log.debug("Credential Payload {}", unsignedCredential);
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

    @Override
    public Mono<Void> retrySignUnsignedCredential(String authorizationHeader, String procedureId) {
        log.info("Retrying to sign credential...");
        return credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId))
                .switchIfEmpty(Mono.error(new RuntimeException("Procedure not found")))
                .flatMap(credentialProcedure -> switch (credentialProcedure.getCredentialType()) {
                    case "VERIFIABLE_CERTIFICATION" ->
                            learCredentialEmployeeFactory.createIssuer(procedureId, VERIFIABLE_CERTIFICATION)
                                    .flatMap(issuer -> verifiableCertificationFactory.mapIssuerAndSigner(procedureId, issuer))
                                    .flatMap(bindCredential -> {
                                        log.info("ProcessID: {} - Credential mapped and bind to the issuer: {}", procedureId, bindCredential);
                                        return credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindCredential, JWT_VC);
                                    });

                    case "LEAR_CREDENTIAL_EMPLOYEE" ->
                            learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(credentialProcedure.getCredentialDecoded(), procedureId)
                                    .flatMap(bindCredential -> {
                                        log.info("ProcessID: {} - Credential mapped and bind to the issuer: {}", procedureId, bindCredential);
                                        return credentialProcedureService.updateDecodedCredentialByProcedureId(procedureId, bindCredential, JWT_VC);
                                    });

                    default -> {
                        log.error("Unknown credential type: {}", credentialProcedure.getCredentialType());
                        yield Mono.error(new IllegalArgumentException("Unsupported credential type: " + credentialProcedure.getCredentialType()));
                    }
                })
                .then(this.signAndUpdateCredentialByProcedureId(authorizationHeader, procedureId, JWT_VC))
                .flatMap(signedVc ->
                        credentialProcedureService.updateCredentialProcedureCredentialStatusToValidByProcedureId(procedureId)
                                .thenReturn(signedVc)
                )
                .flatMap(signedVc -> credentialProcedureRepository.findByProcedureId(UUID.fromString(procedureId))
                        .flatMap(updatedCredentialProcedure -> {
                            updatedCredentialProcedure.setUpdatedAt(Timestamp.from(Instant.now()));
                            return credentialProcedureRepository.save(updatedCredentialProcedure)
                                    .thenReturn(updatedCredentialProcedure);
                        })
                        .flatMap(updatedCredentialProcedure -> {
                            if (!"VERIFIABLE_CERTIFICATION".equals(updatedCredentialProcedure.getCredentialType())) {
                                return Mono.empty(); //don't send message if it isn't VERIFIABLE_CERTIFICATION
                            }

                            return deferredCredentialMetadataService.getResponseUriByProcedureId(procedureId)
                                    .flatMap(responseUri -> {
                                        try {
                                            log.info("Retrieved response URI: " + responseUri);
                                            JsonNode root = new ObjectMapper().readTree(updatedCredentialProcedure.getCredentialDecoded());
                                            String productId = root.get("credentialSubject").get("product").get("productId").asText();
                                            String companyEmail = root.get("credentialSubject").get("company").get("email").asText();

                                            return m2mTokenService.getM2MToken()
                                                    .flatMap(m2mToken -> credentialDeliveryService.sendVcToResponseUri(
                                                            responseUri,
                                                            signedVc,
                                                            m2mToken.accessToken(),
                                                            productId,
                                                            companyEmail
                                                    ));
                                        } catch (Exception e) {
                                            log.error("Error extracting productId or companyEmail from credential", e);
                                            return Mono.error(new RuntimeException("Failed to prepare signed VC for delivery", e));
                                        }
                                    });
                        })
                )
                .then();
    }

}
