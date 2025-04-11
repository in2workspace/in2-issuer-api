package es.in2.issuer.oidc4vci.domain.service.impl;

import com.nimbusds.jose.Payload;
import es.in2.issuer.shared.domain.service.JWTService;
import es.in2.issuer.oidc4vci.domain.model.dto.TokenResponse;
import es.in2.issuer.oidc4vci.domain.service.TokenService;
import es.in2.issuer.shared.domain.model.dto.CredentialIdAndTxCode;
import es.in2.issuer.shared.infrastructure.config.AppConfig;
import es.in2.issuer.shared.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static es.in2.issuer.oidc4vci.domain.util.Constants.ACCESS_TOKEN_EXPIRATION_TIME_DAYS;
import static es.in2.issuer.shared.domain.util.Constants.GRANT_TYPE;
import static es.in2.issuer.shared.domain.util.Constants.PRE_AUTH_CODE_EXPIRY_DURATION_MINUTES;
import static es.in2.issuer.shared.domain.util.Utils.generateCustomNonce;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final CacheStore<CredentialIdAndTxCode> credentialIdAndTxCodeByPreAuthorizedCodeCacheStore;
    private final CacheStore<String> nonceCacheStore;
    private final JWTService jwtService;
    private final AppConfig appConfig;

    @Override
    public Mono<TokenResponse> generateTokenResponse(
            String grantType,
            String preAuthorizedCode,
            String txCode) {

        return ensureGrantTypeIsPreAuthorizedCode(grantType)
                .then(Mono.defer(() -> ensurePreAuthorizedCodeAndTxCodeAreCorrect(preAuthorizedCode, txCode)))
                .then(Mono.defer(this::generateAndSaveNonce)
                        .map(nonce -> {
                            long expirationTimeEpochSeconds = generateAccessTokenExpirationTime();
                            String accessToken = generateAccessToken(expirationTimeEpochSeconds);
                            String tokenType = "bearer";
                            long expiresIn = expirationTimeEpochSeconds - Instant.now().getEpochSecond();
                            System.out.println("expiresIn = " + expiresIn);
                            System.out.println("expirationTimeEpochSeconds = " + expirationTimeEpochSeconds);
                            System.out.println("Instant.now().getEpochSeconds() = " + Instant.now().getEpochSecond());
                            long nonceExpiresIn = (int) TimeUnit.SECONDS.convert(
                                    PRE_AUTH_CODE_EXPIRY_DURATION_MINUTES,
                                    TimeUnit.MINUTES);

                            return TokenResponse.builder()
                                    .accessToken(accessToken)
                                    .tokenType(tokenType)
                                    .expiresIn(expiresIn)
                                    .nonce(nonce)
                                    .nonceExpiresIn(nonceExpiresIn)
                                    .build();
                        }));
    }

    private Mono<String> generateAndSaveNonce() {
        return generateCustomNonce()
                .flatMap(nonce ->
                        nonceCacheStore.add(nonce, nonce));
    }

    private long generateAccessTokenExpirationTime() {
        Instant issueTime = Instant.now();
        return issueTime.plus(
                        ACCESS_TOKEN_EXPIRATION_TIME_DAYS,
                        ChronoUnit.DAYS)
                .getEpochSecond();
    }

    private String generateAccessToken(long expirationTimeEpochSeconds) {
        Payload payload = new Payload(Map.of(
                "iss", appConfig.getIssuerApiExternalDomain(),
                "iat", expirationTimeEpochSeconds,
                "exp", expirationTimeEpochSeconds,
                "jti", UUID.randomUUID()
        ));
        return jwtService.generateJWT(payload.toString());
    }

    private Mono<Void> ensurePreAuthorizedCodeAndTxCodeAreCorrect(String preAuthorizedCode, String txCode) {
        return credentialIdAndTxCodeByPreAuthorizedCodeCacheStore
                .get(preAuthorizedCode)
                .onErrorMap(NoSuchElementException.class, ex -> new IllegalArgumentException("Invalid pre-authorized code"))
                .flatMap(credentialIdAndTxCode ->
                        credentialIdAndTxCode.TxCode().equals(txCode)
                                ? Mono.empty()
                                : Mono.error(new IllegalArgumentException("Invalid tx code"))
                );
    }


    private Mono<Void> ensureGrantTypeIsPreAuthorizedCode(String grantType) {
        return GRANT_TYPE.equals(grantType)
                ? Mono.empty()
                : Mono.error(new IllegalArgumentException("Invalid grant type"));
    }
}
