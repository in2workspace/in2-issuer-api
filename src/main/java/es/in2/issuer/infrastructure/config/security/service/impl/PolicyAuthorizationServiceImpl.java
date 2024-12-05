package es.in2.issuer.infrastructure.config.security.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.InsufficientPermissionException;
import es.in2.issuer.domain.exception.ParseErrorException;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployee;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.domain.util.factory.CredentialFactory;
import es.in2.issuer.infrastructure.config.AuthServerConfig;
import es.in2.issuer.infrastructure.config.VerifierConfig;
import es.in2.issuer.infrastructure.config.security.service.PolicyAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

import static es.in2.issuer.domain.util.Constants.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PolicyAuthorizationServiceImpl implements PolicyAuthorizationService {

    private final JWTService jwtService;
    private final ObjectMapper objectMapper;
    private final AuthServerConfig authServerConfig;
    private final CredentialFactory credentialFactory;
    private final VerifierConfig verifierConfig;

    @Override
    public Mono<Void> authorize(String token, String schema, JsonNode payload) {
        return Mono.fromCallable(() -> jwtService.parseJWT(token))
                .flatMap(signedJWT -> {
                    String vcClaim = jwtService.getClaimFromPayload(signedJWT.getPayload(), "vc");
                    return checkIfCredentialTypeIsAllowedToIssue(vcClaim).flatMap(credentialType -> {
                        if (!LEAR_CREDENTIAL_EMPLOYEE.equals(credentialType)) {
                            return Mono.error(new InsufficientPermissionException("Unauthorized: Credential type not allowed."));
                        }
                        LEARCredentialEmployee learCredential = mapVcToLearCredentialEmployee(vcClaim);
                        return switch (schema) {
                            case LEAR_CREDENTIAL_EMPLOYEE -> authorizeLearCredentialEmployee(learCredential, signedJWT, payload);
                            case VERIFIABLE_CERTIFICATION -> authorizeVerifiableCertification(learCredential, signedJWT);
                            default -> Mono.error(new InsufficientPermissionException("Unauthorized: Unsupported schema"));
                        };
                    });
                });
    }

    private Mono<String> checkIfCredentialTypeIsAllowedToIssue(String vcClaim) {
        return Mono.fromCallable(() -> objectMapper.readTree(vcClaim))
                .flatMap(vcJsonNode -> {
                    JsonNode vcNode = vcJsonNode.get("type");
                    if (vcNode == null) {
                        return Mono.error(new InsufficientPermissionException("The credential type is missing, the credential is invalid."));
                    }
                    if (vcNode.isTextual() && LEAR_CREDENTIAL_EMPLOYEE.equals(vcNode.asText())) {
                        return Mono.just(vcNode.asText());
                    }
                    if (vcNode.isArray()) {
                        for (JsonNode type : vcNode) {
                            if (LEAR_CREDENTIAL_EMPLOYEE.equals(type.asText())) {
                                return Mono.just(type.asText());
                            }
                        }
                    }
                    return Mono.error(new InsufficientPermissionException("Unauthorized: Credential type 'LEARCredentialEmployee' is required."));
                })
                .onErrorMap(JsonProcessingException.class, e -> new ParseErrorException("Error extracting credential type"));
    }


    private LEARCredentialEmployee mapVcToLearCredentialEmployee(String vcClaim) {
        return credentialFactory.learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(vcClaim);
    }

    private Mono<Void> authorizeLearCredentialEmployee(LEARCredentialEmployee learCredential, SignedJWT signedJWT, JsonNode payload) {
        if (isSignerIssuancePolicyValid(learCredential, signedJWT) || isMandatorIssuancePolicyValid(learCredential, payload)) {
            return Mono.empty();
        }
        return Mono.error(new InsufficientPermissionException("Unauthorized: LEARCredentialEmployee does not meet any issuance policies."));
    }

    private Mono<Void> authorizeVerifiableCertification(LEARCredentialEmployee learCredential, SignedJWT signedJWT) {
        if (isVerifiableCertificationPolicyValid(learCredential, signedJWT)) {
            return Mono.empty();
        }
        return Mono.error(new InsufficientPermissionException("Unauthorized: VerifiableCertification does not meet the issuance policy."));
    }

    private boolean isSignerIssuancePolicyValid(LEARCredentialEmployee learCredential, SignedJWT signedJWT) {
        return isTokenIssuedByInternalAuthServer(signedJWT) &&
                isLearCredentialEmployeeMandatorOrganizationIdentifierAllowedSigner(learCredential.credentialSubject().mandate().mandator()) &&
                hasLearCredentialOnboardingExecutePower(learCredential.credentialSubject().mandate().power());
    }

    private boolean isMandatorIssuancePolicyValid(LEARCredentialEmployee learCredential, JsonNode payload) {
        if (!hasLearCredentialOnboardingExecutePower(learCredential.credentialSubject().mandate().power())) {
            return false;
        }
        LEARCredentialEmployee.CredentialSubject.Mandate mandate = objectMapper.convertValue(payload, LEARCredentialEmployee.CredentialSubject.Mandate.class);
        return mandate != null &&
                mandate.mandator().equals(learCredential.credentialSubject().mandate().mandator()) &&
                payloadPowersOnlyIncludeProductOffering(mandate.power());
    }

    private boolean isVerifiableCertificationPolicyValid(LEARCredentialEmployee learCredential, SignedJWT signedJWT) {
        return containsCertificationAndAttest(learCredential.credentialSubject().mandate().power()) &&
                isTokenIssuedByExternalVerifier(signedJWT);
    }

    private boolean containsCertificationAndAttest(List<LEARCredentialEmployee.CredentialSubject.Mandate.Power> powers) {
        return powers.stream().anyMatch(this::isCertificationFunction) &&
                powers.stream().anyMatch(this::hasAttestAction);
    }

    private boolean isCertificationFunction(LEARCredentialEmployee.CredentialSubject.Mandate.Power power) {
        return "Certification".equals(power.tmfFunction());
    }

    private boolean hasAttestAction(LEARCredentialEmployee.CredentialSubject.Mandate.Power power) {
        return power.tmfAction() instanceof List<?> actions ?
                actions.stream().anyMatch(action -> "Attest".equals(action.toString())) :
                "Attest".equals(power.tmfAction().toString());
    }

    private boolean isTokenIssuedByInternalAuthServer(SignedJWT signedJWT) {
        return authServerConfig.getJwtValidator().equals(signedJWT.getPayload().toJSONObject().get("iss").toString());
    }

    private boolean isTokenIssuedByExternalVerifier(SignedJWT signedJWT) {
        return verifierConfig.getVerifierExternalDomain().equals(signedJWT.getPayload().toJSONObject().get("iss").toString());
    }

    private boolean hasLearCredentialOnboardingExecutePower(List<LEARCredentialEmployee.CredentialSubject.Mandate.Power> powers) {
        return powers.stream().anyMatch(this::isOnboardingFunction) &&
                powers.stream().anyMatch(this::hasExecuteAction);
    }

    private boolean isOnboardingFunction(LEARCredentialEmployee.CredentialSubject.Mandate.Power power) {
        return "Onboarding".equals(power.tmfFunction());
    }

    private boolean hasExecuteAction(LEARCredentialEmployee.CredentialSubject.Mandate.Power power) {
        return power.tmfAction() instanceof List<?> actions ?
                actions.stream().anyMatch(action -> "Execute".equals(action.toString())) :
                "Execute".equals(power.tmfAction().toString());
    }

    private boolean isLearCredentialEmployeeMandatorOrganizationIdentifierAllowedSigner(LEARCredentialEmployee.CredentialSubject.Mandate.Mandator mandator) {
        return IN2_ORGANIZATION_IDENTIFIER.equals(mandator.organizationIdentifier());
    }

    private boolean payloadPowersOnlyIncludeProductOffering(List<LEARCredentialEmployee.CredentialSubject.Mandate.Power> powers) {
        return powers.stream().allMatch(power -> "ProductOffering".equals(power.tmfFunction()));
    }
}