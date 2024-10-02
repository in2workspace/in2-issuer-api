package es.in2.issuer.domain.service.impl;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.JWTVerificationException;
import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import es.in2.issuer.domain.service.DIDService;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.domain.service.M2MTokenService;
import es.in2.issuer.infrastructure.config.VerifierConfig;
import es.in2.issuer.infrastructure.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
                OAuth2ParameterNames.CLIENT_ID, verifierConfig.getCredentialSubjectKey(),
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
    public Mono<Void> verifyM2MToken(String m2mAccessToken) {
        return Mono.fromCallable(() -> JWSObject.parse(m2mAccessToken))
                .flatMap(jwtService::validateJwtSignatureReactive)
                .flatMap(isValid -> {
                    if (Boolean.TRUE.equals(isValid)) {
                        log.info("M2MTokenServiceImpl -- verifyM2MToken -- IS VALID ?: {}", true);
                        return Mono.empty();
                    } else {
                        return Mono.error(new JWTVerificationException("Token signature is invalid"));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error while verifying M2M token", e);
                    return Mono.error(e);
                }).then();
    }


}
