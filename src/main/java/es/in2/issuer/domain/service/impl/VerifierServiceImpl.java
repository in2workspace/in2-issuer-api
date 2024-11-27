package es.in2.issuer.domain.service.impl;

import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.JWTVerificationException;
import es.in2.issuer.domain.exception.TokenFetchException;
import es.in2.issuer.domain.exception.WellKnownInfoFetchException;
import es.in2.issuer.domain.model.dto.OpenIDProviderMetadata;
import es.in2.issuer.domain.model.dto.VerifierOauth2AccessToken;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.domain.service.VerifierService;
import es.in2.issuer.infrastructure.config.VerifierConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static es.in2.issuer.domain.util.Constants.CONTENT_TYPE;
import static es.in2.issuer.domain.util.Constants.CONTENT_TYPE_URL_ENCODED_FORM;

@Service
@Slf4j
@RequiredArgsConstructor
public class VerifierServiceImpl implements VerifierService {

    private final VerifierConfig verifierConfig;
    private final JWTService jwtService;
    private final WebClient oauth2VerifierWebClient;

    @Override
    public Mono<Void> verifyToken(String accessToken) {
        return Mono.fromCallable(() -> JWSObject.parse(accessToken))
                .flatMap(jwtService::validateJwtSignatureReactive)
                .flatMap(isValid -> {
                    String issuerDidKey = extractIssuerFromToken(accessToken);
                    Long expirationEpoch = jwtService.getExpirationFromToken(accessToken);
                    Instant expirationInstant = Instant.ofEpochSecond(expirationEpoch);
                    if (Boolean.TRUE.equals(isValid)
                            && verifierConfig.getDidKey().equals(issuerDidKey)
                            && expirationInstant.isAfter(Instant.now())) {
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

    @Override
    public Mono<OpenIDProviderMetadata> getWellKnownInfo(){
        String wellKnownInfoEndpoint = verifierConfig.getVerifierExternalDomain() + verifierConfig.getVerifierWellKnownPath();

        return oauth2VerifierWebClient.get()
                .uri(wellKnownInfoEndpoint)
                .retrieve()
                .bodyToMono(OpenIDProviderMetadata.class)
                .onErrorResume(e -> Mono.error(new WellKnownInfoFetchException("Error fetching OpenID Provider Metadata", e)));
    }

    @Override
    public Mono<VerifierOauth2AccessToken> performTokenRequest(String body) {
        return getWellKnownInfo()
                .flatMap(metadata -> {
                    String tokenEndpoint = metadata.tokenEndpoint();
                    return oauth2VerifierWebClient.post()
                            .uri(tokenEndpoint)
                            .header(CONTENT_TYPE, CONTENT_TYPE_URL_ENCODED_FORM)
                            .bodyValue(body)
                            .retrieve()
                            .bodyToMono(VerifierOauth2AccessToken.class)
                            .onErrorMap(e -> new TokenFetchException("Error fetching token", e));
                });
    }


    private String extractIssuerFromToken(String token) {
        SignedJWT signedToken = jwtService.parseJWT(token);
        Payload payload = jwtService.getPayloadFromSignedJWT(signedToken);
        return jwtService.getClaimFromPayload(payload,"iss");
    }

}