package es.in2.issuer.infrastructure.config.security.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.InsufficientPermissionException;
import es.in2.issuer.domain.exception.ParseErrorException;
import es.in2.issuer.domain.model.dto.credential.lear.LEARCredential;
import es.in2.issuer.domain.model.dto.credential.lear.Mandator;
import es.in2.issuer.domain.model.dto.credential.lear.Power;
import es.in2.issuer.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.domain.util.factory.CredentialFactory;
import es.in2.issuer.infrastructure.config.security.service.PolicyAuthorizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.StreamSupport;

import static es.in2.issuer.domain.util.Constants.*;
import static es.in2.issuer.domain.util.Utils.extractMandator;
import static es.in2.issuer.domain.util.Utils.extractPowers;

@Service
@Slf4j
@RequiredArgsConstructor
public class PolicyAuthorizationServiceImpl implements PolicyAuthorizationService {

    private final JWTService jwtService;
    private final ObjectMapper objectMapper;
    private final CredentialFactory credentialFactory;

    @Override
    public Mono<Void> authorize(String token, String schema, JsonNode payload) {
        return Mono.fromCallable(() -> jwtService.parseJWT(token))
                .flatMap(signedJWT -> Mono.justOrEmpty(jwtService.getClaimFromPayload(signedJWT.getPayload(), ROLE))
                        .switchIfEmpty(Mono.error(new SecurityException("Access denied: Unauthorized Rol 'null'")))
                        .flatMap(role -> switch(role.replace("\"", "")) {
                                case SYSDAMIN, LER -> Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                        "The request is invalid. The roles 'SYSADMIN' and 'LER' currently have no defined permissions."));
                                case LEAR -> handleLearRole(token, schema, payload);
                                default ->
                                        Mono.error(new SecurityException("Access denied: Unauthorized Rol '" + role + "'"));
                            }
                        )
                );
    }

    private Mono<Void> handleLearRole(String token, String schema, JsonNode payload) {
        return Mono.fromCallable(() -> jwtService.parseJWT(token))
            .flatMap(signedJWT -> {
                String vcClaim = jwtService.getClaimFromPayload(signedJWT.getPayload(), VC);
                return mapVcToLEARCredential(vcClaim, schema)
                    .flatMap(learCredential -> switch (schema) {
                        case LEAR_CREDENTIAL_EMPLOYEE -> authorizeLearCredentialEmployee(learCredential, payload);
                        case VERIFIABLE_CERTIFICATION -> authorizeVerifiableCertification(learCredential);
                        default -> Mono.error(new InsufficientPermissionException("Unauthorized: Unsupported schema"));
                    });
            });
    }

    /**
     * Determines the allowed credential type based on the provided list and schema.
     * Returns a Mono emitting the allowed type.
     */
    private Mono<String> determineAllowedCredentialType(List<String> types, String schema) {
        return Mono.fromCallable(() -> {
            if (VERIFIABLE_CERTIFICATION.equals(schema)) {
                // For verifiable certification, only LEARCredentialEmployee is allowed.
                if (types.contains(LEAR_CREDENTIAL_EMPLOYEE)) {
                    return LEAR_CREDENTIAL_EMPLOYEE;
                } else {
                    throw new InsufficientPermissionException(
                            "Unauthorized: Credential type 'LEARCredentialEmployee' is required for verifiable certification.");
                }
            } else {
                // For LEAR_CREDENTIAL_EMPLOYEE schema, allow either employee or machine.
                if (types.contains(LEAR_CREDENTIAL_EMPLOYEE)) {
                    return LEAR_CREDENTIAL_EMPLOYEE;
                } else if (types.contains(LEAR_CREDENTIAL_MACHINE)) {
                    return LEAR_CREDENTIAL_MACHINE;
                } else {
                    throw new InsufficientPermissionException(
                            "Unauthorized: Credential type 'LEARCredentialEmployee' or 'LEARCredentialMachine' is required.");
                }
            }
        });
    }

    private Mono<LEARCredential> mapVcToLEARCredential(String vcClaim, String schema) {
        return checkIfCredentialTypeIsAllowedToIssue(vcClaim, schema)
                .flatMap(credentialType -> {
                    if (LEAR_CREDENTIAL_EMPLOYEE.equals(credentialType)) {
                        return Mono.fromCallable(() -> credentialFactory.learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(vcClaim));
                    } else if (LEAR_CREDENTIAL_MACHINE.equals(credentialType)) {
                        return Mono.fromCallable(() -> credentialFactory.learCredentialMachineFactory.mapStringToLEARCredentialMachine(vcClaim));
                    } else {
                        return Mono.error(new InsufficientPermissionException("Unsupported credential type: " + credentialType));
                    }
                });
    }

    /**
     * Checks if the credential type contained in the vcClaim is allowed for the given schema.
     * Returns a Mono emitting the allowed type if valid, or an error otherwise.
     */
    private Mono<String> checkIfCredentialTypeIsAllowedToIssue(String vcClaim, String schema) {
        return Mono.fromCallable(() -> objectMapper.readTree(vcClaim))
                .flatMap(vcJsonNode ->
                        extractCredentialTypes(vcJsonNode)
                                .flatMap(types -> determineAllowedCredentialType(types, schema))
                )
                .onErrorMap(JsonProcessingException.class, e -> new ParseErrorException("Error extracting credential type"));
    }

    /**
     * Extracts and validates the credential types from the provided JSON node.
     * Returns a Mono emitting the list of credential type strings.
     */
    private Mono<List<String>> extractCredentialTypes(JsonNode vcJsonNode) {
        return Mono.fromCallable(() -> {
            JsonNode typeNode = vcJsonNode.get("type");
            if (typeNode == null) {
                throw new InsufficientPermissionException("The credential type is missing, the credential is invalid.");
            }
            if (typeNode.isTextual()) {
                return List.of(typeNode.asText());
            } else if (typeNode.isArray()) {
                return StreamSupport.stream(typeNode.spliterator(), false)
                        .map(JsonNode::asText)
                        .toList();
            } else {
                throw new InsufficientPermissionException("Invalid format for credential type.");
            }
        });
    }

    private Mono<Void> authorizeLearCredentialEmployee(LEARCredential learCredential, JsonNode payload) {
        if (isSignerIssuancePolicyValid(learCredential) || isMandatorIssuancePolicyValid(learCredential, payload)) {
            return Mono.empty();
        }
        return Mono.error(new InsufficientPermissionException("Unauthorized: LEARCredentialEmployee does not meet any issuance policies."));
    }

    private Mono<Void> authorizeVerifiableCertification(LEARCredential learCredential) {
        if (isVerifiableCertificationPolicyValid(learCredential)) {
            return Mono.empty();
        }
        return Mono.error(new InsufficientPermissionException("Unauthorized: VerifiableCertification does not meet the issuance policy."));
    }

    private boolean isSignerIssuancePolicyValid(LEARCredential learCredential) {
        return isLearCredentialEmployeeMandatorOrganizationIdentifierAllowedSigner(extractMandator(learCredential)) &&
                hasLearCredentialOnboardingExecutePower(extractPowers(learCredential));
    }

    private boolean isMandatorIssuancePolicyValid(LEARCredential learCredential, JsonNode payload) {
        if (!hasLearCredentialOnboardingExecutePower(extractPowers(learCredential))) {
            return false;
        }
        LEARCredentialEmployee.CredentialSubject.Mandate mandate = objectMapper.convertValue(payload, LEARCredentialEmployee.CredentialSubject.Mandate.class);
        return mandate != null &&
                mandate.mandator().equals(extractMandator(learCredential)) &&
                payloadPowersOnlyIncludeProductOffering(mandate.power());
    }

    private boolean isVerifiableCertificationPolicyValid(LEARCredential learCredential) {
        return containsCertificationAndAttest(extractPowers(learCredential));
    }

    private boolean containsCertificationAndAttest(List<Power> powers) {
        return powers.stream().anyMatch(this::isCertificationFunction) &&
                powers.stream().anyMatch(this::hasAttestAction);
    }

    private boolean isCertificationFunction(Power power) {
        return "Certification".equals(power.function());
    }

    private boolean hasAttestAction(Power power) {
        return power.action() instanceof List<?> actions ?
                actions.stream().anyMatch(action -> "Attest".equals(action.toString())) :
                "Attest".equals(power.action().toString());
    }

    private boolean hasLearCredentialOnboardingExecutePower(List<Power> powers) {
        return powers.stream().anyMatch(this::isOnboardingFunction) &&
                powers.stream().anyMatch(this::hasExecuteAction);
    }

    private boolean isOnboardingFunction(Power power) {
        return "Onboarding".equals(power.function());
    }

    private boolean hasExecuteAction(Power power) {
        return power.action() instanceof List<?> actions ?
                actions.stream().anyMatch(action -> "Execute".equals(action.toString())) :
                "Execute".equals(power.action().toString());
    }

    private boolean isLearCredentialEmployeeMandatorOrganizationIdentifierAllowedSigner(Mandator mandator) {
        return IN2_ORGANIZATION_IDENTIFIER.equals(mandator.organizationIdentifier());
    }

    private boolean payloadPowersOnlyIncludeProductOffering(List<Power> powers) {
        return powers.stream().allMatch(power -> "ProductOffering".equals(power.function()));
    }
}