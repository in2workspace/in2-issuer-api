package es.in2.issuer.domain.service.impl;

import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.model.dto.CustomJWK;
import es.in2.issuer.domain.model.dto.CustomJWKS;
import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import es.in2.issuer.domain.model.enums.KeyType;
import es.in2.issuer.domain.service.DIDService;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.domain.service.M2MTokenService;
import es.in2.issuer.infrastructure.config.VerifierConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.security.AlgorithmParameters;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static es.in2.issuer.domain.util.Constants.*;
import static es.in2.issuer.domain.util.Constants.CLIENT_ASSERTION_TYPE_VALUE;

@Service
@Slf4j
@RequiredArgsConstructor
public class M2MTokenServiceImpl implements M2MTokenService {

    private final WebClient oauth2VerifierWebClient;
    private final VerifierConfig verifierConfig;
    private final JWTService jwtService;
    private final DIDService didService;

    @Override
    public Mono<VerifierOauth2AccessToken> getM2MToken() {
        return oauth2VerifierWebClient.post()
                .uri(verifierConfig.getVerifierPathsTokenPath())
                .header(CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED_FORM)
                .bodyValue(getM2MFormUrlEncodeBodyValue())
                .retrieve()
                .bodyToMono(VerifierOauth2AccessToken.class)
                .onErrorResume(e -> Mono.error(new RuntimeException("Error fetching token", e)));
    }

    private String getM2MFormUrlEncodeBodyValue() {
        Map<String, String> parameters = Map.of(
                OAuth2ParameterNames.GRANT_TYPE, CLIENT_CREDENTIALS_GRANT_TYPE_VALUE,
                OAuth2ParameterNames.CLIENT_ID, verifierConfig.getVerifierKey(),
                OAuth2ParameterNames.CLIENT_ASSERTION_TYPE, CLIENT_ASSERTION_TYPE_VALUE,
                OAuth2ParameterNames.CLIENT_ASSERTION, createClientAssertion()
        );

        return parameters.entrySet()
                .stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
    }

    private String createClientAssertion() {

        String vcMachineString = getVCinJWTDecodedFromBase64();
        SignedJWT vcMachineJWT = jwtService.parseJWT(vcMachineString);
        Payload vcMachinePayload = jwtService.getPayloadFromSignedJWT(vcMachineJWT);
        String clientId = jwtService.getClaimFromPayload(vcMachinePayload,"sub");

        Instant issueTime = Instant.now();
        long iat = issueTime.toEpochMilli();
        long exp = issueTime.plus(
                Long.parseLong(verifierConfig.getVerifierClientAssertionTokenExpiration()), // Amount to add
                ChronoUnit.valueOf(verifierConfig.getVerifierClientAssertionTokenCronUnit())).toEpochMilli(); // Chron Unit

        String vpTokenJWTString = createVPTokenJWT(vcMachineString,clientId, iat, exp);



        Payload payload = new Payload(Map.of(
                "sub", clientId,
                "iss", clientId,
                "aud", verifierConfig.getVerifierExternalDomain(),
                "iat", iat,
                "exp", exp,
                "jti", UUID.randomUUID(),
                "vp_token", vpTokenJWTString
        ));

        return jwtService.generateJWT(payload.toString());
    }

    private String createVPTokenJWT(String vcMachineString, String clientId, long iat, long exp) {
        Map<String, Object> vp = createVP(vcMachineString,clientId);

        Payload payload = new Payload(Map.of(
                "sub", clientId,
                "iss", clientId,
                "nbf", iat, // The same value of Issue Time
                "iat", iat,
                "exp", exp,
                "jti", UUID.randomUUID(),
                "vp", vp
        ));

        return jwtService.generateJWT(payload.toString());

    }

    private Map<String, Object> createVP(String vcMachineString, String clientId){
        return Map.of(
                "@context", List.of("https://www.w3.org/2018/credentials/v1"),
                "holder", clientId,
                "id", "urn:uuid:" + UUID.randomUUID(),
                "type", List.of("VerifiablePresentation"),
                "verifiableCredential", List.of(vcMachineString)
        );
    }

    private String getVCinJWTDecodedFromBase64(){
        String vcTokenBase64 = verifierConfig.getVerifierVc();
        byte [] vcTokenDecoded = Base64.getDecoder().decode(vcTokenBase64);
        return new String(vcTokenDecoded);
    }

    @Override
    public Mono<Void> verifyM2MToken(String token) {

        String uri = "http://localhost:9000" + verifierConfig.getVerifierPathsResolveDidPath() + "/" + verifierConfig.getVerifierKey();

        Mono<CustomJWKS> jwksMono = oauth2VerifierWebClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(CustomJWKS.class);

        log.info(jwksMono.toString());

        return jwksMono.flatMap(customJWKS -> {
                    try {
                        PublicKey publicKey = didService.getPublicKeyFromDid(""); //TODO Implement this new version
                        // createPublicKeyFromJWK(customJWKS.keys().get(0));

                        jwtService.verifyJWTSignature(token,publicKey, KeyType.EC);

                        return Mono.empty();

                    } catch (Exception e) {
                        log.error("Error during signature verification", e);
                        return Mono.error(new RuntimeException(e));
                    }
                })
                .onErrorResume(error -> {
                    // Manejar errores de la llamada web o del procesamiento del token
                    log.info("\n==================================\n");
                    log.error("[WebClientException:{}]", error.getMessage());
                    log.info("\n==================================\n");

                    return Mono.error(error);
                }).then();
    }

    private PublicKey createPublicKeyFromJWK(CustomJWK customJWK) throws Exception {

        byte[] xDecoded = Base64.getUrlDecoder().decode(customJWK.x());
        byte[] yDecoded = Base64.getUrlDecoder().decode(customJWK.y());

        ECPoint ecPoint = new ECPoint(new BigInteger(1, xDecoded), new BigInteger(1, yDecoded));

        ECParameterSpec ecParameterSpec = getECParameterSpec(customJWK.crv(), customJWK.kty());

        ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(ecPoint, ecParameterSpec);

        KeyFactory keyFactory = KeyFactory.getInstance(customJWK.kty());

        return keyFactory.generatePublic(pubKeySpec);
    }

    private ECParameterSpec getECParameterSpec(String curve, String alg) throws Exception {
        if ("P-256".equals(curve)) {
            AlgorithmParameters parameters = AlgorithmParameters.getInstance(alg);
            parameters.init(new ECGenParameterSpec("secp256r1"));
            return parameters.getParameterSpec(ECParameterSpec.class);
        } else {
            throw new IllegalArgumentException("Unsupported curve: " + curve);
        }
    }

}
