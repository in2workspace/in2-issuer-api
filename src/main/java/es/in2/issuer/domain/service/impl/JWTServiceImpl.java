package es.in2.issuer.domain.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.JWTClaimMissingException;
import es.in2.issuer.domain.exception.JWTCreationException;
import es.in2.issuer.domain.exception.JWTParsingException;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.infrastructure.crypto.CryptoComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JWTServiceImpl implements JWTService {

    private final ObjectMapper objectMapper;
    private final CryptoComponent cryptoComponent;

    @Override
    public String generateJWT(String payload) {
        try {
            // Get ECKey
            ECKey ecJWK = cryptoComponent.getECKey();
            // Set Header
            JWSHeader jwsHeader = new JWSHeader.Builder(JWSAlgorithm.ES256)
                    .keyID(cryptoComponent.getECKey().getKeyID())
                    .type(JOSEObjectType.JWT)
                    .build();
            // Set Payload
            JWTClaimsSet claimsSet = convertPayloadToJWTClaimsSet(payload);
            // Create JWT for ES256R algorithm
            SignedJWT jwt = new SignedJWT(jwsHeader, claimsSet);
            // Sign with a private EC key
            JWSSigner signer = new ECDSASigner(ecJWK);
            jwt.sign(signer);
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new JWTCreationException("Error creating JWT");
        }
    }

    private JWTClaimsSet convertPayloadToJWTClaimsSet(String payload) {
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            Map<String, Object> claimsMap = objectMapper.convertValue(jsonNode, new TypeReference<>() {
            });
            JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
            for (Map.Entry<String, Object> entry : claimsMap.entrySet()) {
                builder.claim(entry.getKey(), entry.getValue());
            }
            return builder.build();
        } catch (JsonProcessingException e) {
            log.error("Error while parsing the JWT payload", e);
            throw new JWTCreationException("Error while parsing the JWT payload");
        }

    }

    @Override
    public SignedJWT parseJWT(String jwt) {
        try {
            return SignedJWT.parse(jwt);
        } catch (ParseException e) {
            log.error("Error al parsear el JWTs: {}", e.getMessage());
            throw new JWTParsingException("Error al parsear el JWTs");
        }
    }

    @Override
    public Payload getPayloadFromSignedJWT(SignedJWT signedJWT) {
        return signedJWT.getPayload();
    }

    @Override
    public String getClaimFromPayload(Payload payload, String claimName) {
        String claimValue = (String) payload.toJSONObject().get(claimName);
        if (claimValue == null || claimValue.trim().isEmpty()) {
            throw new JWTClaimMissingException(String.format("The '%s' claim is missing or empty in the JWT payload.", claimName));
        }
        return claimValue;
    }
}
