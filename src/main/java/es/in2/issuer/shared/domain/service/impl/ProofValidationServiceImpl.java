package es.in2.issuer.shared.domain.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSObject;
import es.in2.issuer.shared.domain.exception.NonceValidationException;
import es.in2.issuer.shared.domain.exception.ProofValidationException;
import es.in2.issuer.shared.domain.model.dto.NonceValidationResponse;
import es.in2.issuer.shared.domain.exception.ParseErrorException;
import es.in2.issuer.shared.domain.service.JWTService;
import es.in2.issuer.shared.domain.service.ProofValidationService;
import es.in2.issuer.shared.infrastructure.config.AuthServerConfig;
import es.in2.issuer.shared.infrastructure.config.WebClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static es.in2.issuer.backoffice.domain.util.Constants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProofValidationServiceImpl implements ProofValidationService {

    private final WebClientConfig webClient;
    private final ObjectMapper objectMapper;
    private final AuthServerConfig authServerConfig;
    private final JWTService jwtService;


    @Override
    public Mono<Boolean> isProofValid(String jwtProof, String token) {
        return Mono.just(jwtProof)
                .doOnNext(jwt -> log.debug("Starting validation for JWT: {}", jwt))
                .flatMap(this::parseAndValidateJwt)
                .doOnNext(jws -> log.debug("JWT parsed successfully"))
                .flatMap(jwsObject ->
                        jwtService.validateJwtSignatureReactive(jwsObject)
                                .doOnSuccess(isSignatureValid -> log.debug("Signature validation result: {}", isSignatureValid))
                                .map(isSignatureValid -> Boolean.TRUE.equals(isSignatureValid) ? jwsObject : null)
                )
                .doOnNext(jwsObject -> {
                    if (jwsObject == null) log.debug("JWT signature validation failed");
                    else log.debug("JWT signature validated, checking nonce...");
                })
                .flatMap(jwsObject ->
//                        jwsObject != null ? isNoncePresentInCache(jwsObject) : Mono.just(false)
                                isNonceValid(jwsObject, token)
                )
                .map(NonceValidationResponse::isNonceValid)
                .doOnSuccess(result -> log.debug("Final validation result: {}", result))
                .onErrorMap(e -> new ProofValidationException("Error during JWT validation"));
    }

    private Mono<JWSObject> parseAndValidateJwt(String jwtProof) {
        return Mono.fromCallable(() -> {
            JWSObject jwsObject = JWSObject.parse(jwtProof);
            validateHeader(jwsObject);
            validatePayload(jwsObject);
            return jwsObject;
        });
    }

    private void validateHeader(JWSObject jwsObject) {
        Map<String, Object> headerParams = jwsObject.getHeader().toJSONObject();
        if (headerParams.get("alg") == null || headerParams.get("typ") == null ||
                !SUPPORTED_PROOF_ALG.equals(headerParams.get("alg")) ||
                !SUPPORTED_PROOF_TYP.equals(headerParams.get("typ"))) {
            throw new IllegalArgumentException("Invalid JWT header");
        }
    }

    private void validatePayload(JWSObject jwsObject) {
        var payload = jwsObject.getPayload().toJSONObject();
        if (!payload.containsKey("aud") || !payload.containsKey("iat") ||
                Instant.now().isAfter(Instant.ofEpochSecond(Long.parseLong(payload.get("exp").toString())))) {
            throw new IllegalArgumentException("Invalid JWT payload");
        }
    }

    private Mono<NonceValidationResponse> isNonceValid(JWSObject jwsObject, String token) {
        var payload = jwsObject.getPayload().toJSONObject();
        String nonce = payload.get("nonce").toString();
        Map<String, String> formDataMap = Map.of("nonce", nonce);

        // Build the request body
        String xWwwFormUrlencodedBody = formDataMap.entrySet().stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        return webClient.commonWebClient()
                .post()
                .uri(authServerConfig.getAuthServerInternalDomain() + authServerConfig.getAuthServerNonceValidationPath())
                .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + token)
                .bodyValue(xWwwFormUrlencodedBody)
                .exchangeToMono(response -> {
                    if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
                        return Mono.error(new NonceValidationException("There was an error during the validation of nonce, error" + response));
                    } else if (response.statusCode().is3xxRedirection()) {
                        return Mono.just(Objects.requireNonNull(response.headers().asHttpHeaders().getFirst(HttpHeaders.LOCATION)));
                    } else {
                        return response.bodyToMono(String.class);
                    }
                })
                // Parsing response
                .flatMap(response -> {
                    try {
                        return Mono.just(objectMapper.readValue(response, NonceValidationResponse.class));
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        return Mono.error(new ParseErrorException("Error parsing JSON response"));
                    }
                });
    }

}
