package es.in2.issuer.infrastructure.config.security.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployee;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.infrastructure.config.security.service.PolicyAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

import static es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL_EMPLOYEE;
import static es.in2.issuer.domain.util.Constants.VERIFIABLE_CERTIFICATION;

@Service
@RequiredArgsConstructor
public class PolicyAuthorizationServiceImpl implements PolicyAuthorizationService {

    private final JWTService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> authorize(String authorizationHeader, String schema) {
        return jwtService.parseJwtVCAsJsonNode(authorizationHeader)
                .flatMap(parsedJwt -> {
                    // Policy 1: If the credential is LEARCredentialEmployee and schema is LEARCredentialEmployee
                    if (isLEARCredentialEmployee(parsedJwt) && schema.equals(LEAR_CREDENTIAL_EMPLOYEE)) {
                        return Mono.empty(); // Authorization granted
                    }
                    // Policy 2: If the credential is LEARCredentialEmployee and schema is VerifiableCertification
                    if (isLEARCredentialEmployee(parsedJwt) && schema.equals(VERIFIABLE_CERTIFICATION)) {
                        return processLearCredentialEmployeePolicy(parsedJwt);
                    }
                    // If neither policy matches, throw an exception
                    return Mono.error(new AuthorizationServiceException("Unauthorized: Your credential does not match the requirements."));
                });
    }

    private boolean isLEARCredentialEmployee(JsonNode parsedJwt) {
        // Check if the credential type includes "LEARCredentialEmployee"
        JsonNode typeNode = parsedJwt.get("type");
        if (typeNode != null && typeNode.isArray()) {
            for (JsonNode type : typeNode) {
                if (LEAR_CREDENTIAL_EMPLOYEE.equals(type.asText())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Mono<Void> processLearCredentialEmployeePolicy(JsonNode parsedJwt) {
        try {
            // Map the "vc" object to LEARCredentialEmployee
            LEARCredentialEmployee learCredential = objectMapper.treeToValue(parsedJwt, LEARCredentialEmployee.class);

            // Check if any power contains tmf_function "Certification" and tmf_action includes "Attest"
            if (learCredential.credentialSubject() != null
                    && learCredential.credentialSubject().mandate() != null
                    && containsCertificationAndAttest(learCredential.credentialSubject().mandate().power())) {
                return Mono.empty(); // Authorization granted
            } else {
                return Mono.error(new AuthorizationServiceException("Unauthorized: The credential does not meet the power requirements."));
            }
        } catch (Exception e) {
            return Mono.error(new AuthorizationServiceException("Error processing the credential: " + e.getMessage(), e));
        }
    }

    private boolean containsCertificationAndAttest(List<LEARCredentialEmployee.CredentialSubject.Mandate.Power> powers) {
        return powers.stream()
                .filter(this::isCertificationFunction)
                .anyMatch(this::hasAttestAction);
    }

    private boolean isCertificationFunction(LEARCredentialEmployee.CredentialSubject.Mandate.Power power) {
        return "Certification".equals(power.tmfFunction());
    }

    private boolean hasAttestAction(LEARCredentialEmployee.CredentialSubject.Mandate.Power power) {
        if (power.tmfAction() instanceof List<?> actions) {
            return actions.stream().anyMatch(action -> "Attest".equals(action.toString()));
        } else {
            return "Attest".equals(power.tmfAction().toString());
        }
    }

}


