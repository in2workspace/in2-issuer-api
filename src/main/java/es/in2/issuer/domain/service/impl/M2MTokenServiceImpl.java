package es.in2.issuer.domain.service.impl;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.JWTVerificationException;
import es.in2.issuer.domain.exception.TokenFetchException;
import es.in2.issuer.domain.exception.VerifierConfigurationException;
import es.in2.issuer.domain.exception.WellKnownInfoFetchException;
import es.in2.issuer.domain.model.dto.VerifierConfiguration;
import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.domain.service.M2MTokenService;
import es.in2.issuer.infrastructure.config.VerifierConfig;
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

    @Override
    public Mono<VerifierOauth2AccessToken> getM2MToken() {
        return getWellKnownInfo()
                .flatMap(verifierConfiguration -> {
                    if (verifierConfiguration == null) {
                        return Mono.error(new RuntimeException("Verifier configuration is null"));
                    }
                    return oauth2VerifierWebClient.post()
                            .uri(verifierConfiguration.tokenEndpoint())
                            .header(CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED_FORM)
                            .bodyValue(getM2MFormUrlEncodeBodyValue())
                            .retrieve()
                            .bodyToMono(VerifierOauth2AccessToken.class)
                            .onErrorMap(e -> new TokenFetchException("Error fetching token", e));
                })
                .switchIfEmpty(Mono.error(new VerifierConfigurationException("Failed to retrieve verifier configuration")));
    }


    private Mono<VerifierConfiguration> getWellKnownInfo(){
        String wellKnownInfoEndpoint = verifierConfig.getVerifierExternalDomain() + verifierConfig.getVerifierWellKnownPath();

        return oauth2VerifierWebClient.get()
                .uri(wellKnownInfoEndpoint)
                .retrieve()
                .bodyToMono(VerifierConfiguration.class)
                .onErrorResume(e -> Mono.error(new WellKnownInfoFetchException("Error fetching well known data", e)));
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
                    String issuerDidKey = extractIssuerFromToken(m2mAccessToken);
                    if (Boolean.TRUE.equals(isValid) && verifierConfig.getVerifierDidKey().equals(issuerDidKey)) {
                        log.info("M2MTokenServiceImpl -- verifyM2MToken -- IS VALID ?: {}", true);
                        return Mono.empty();
                    } else {
                        return Mono.error(new JWTVerificationException("Token is invalid"));
                    }
                })
                .onErrorResume(e -> {
                    log.error("Error while verifying M2M token", e);
                    return Mono.error(e);
                }).then();
    }

    private String extractIssuerFromToken(String token) {
        SignedJWT signedToken = jwtService.parseJWT(token);
        Payload payload = jwtService.getPayloadFromSignedJWT(signedToken);
        return jwtService.getClaimFromPayload(payload,"iss");
    }


}
