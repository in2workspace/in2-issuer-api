package es.in2.issuer.infrastructure.config.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.InsufficientPermissionException;
import es.in2.issuer.domain.exception.ParseErrorException;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployee;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.domain.util.factory.CredentialFactory;
import es.in2.issuer.domain.util.factory.LEARCredentialEmployeeFactory;
import es.in2.issuer.domain.util.factory.VerifiableCertificationFactory;
import es.in2.issuer.infrastructure.config.AuthServerConfig;
import es.in2.issuer.infrastructure.config.VerifierConfig;
import es.in2.issuer.infrastructure.config.security.service.impl.PolicyAuthorizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static es.in2.issuer.domain.util.Constants.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyAuthorizationServiceImplTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AuthServerConfig authServerConfig;


    @Mock
    private LEARCredentialEmployeeFactory learCredentialEmployeeFactory;
    @Mock
    private VerifiableCertificationFactory verifiableCertificationFactory;

    @Mock
    private VerifierConfig verifierConfig;

    @InjectMocks
    private PolicyAuthorizationServiceImpl policyAuthorizationService;

    @BeforeEach
    void setUp() {
        // Creamos una instancia real de CredentialFactory, pasando los mocks necesarios
        CredentialFactory credentialFactory = new CredentialFactory(learCredentialEmployeeFactory, verifiableCertificationFactory);

        // Inicializamos policyAuthorizationService con las dependencias adecuadas
        policyAuthorizationService = new PolicyAuthorizationServiceImpl(
                jwtService,
                objectMapper,
                authServerConfig,
                credentialFactory,
                verifierConfig
        );
    }

    @Test
    void authorize_success_withLearCredentialEmployee() throws Exception {
        // Arrange
        String token = "valid-token";
        JsonNode payload = mock(JsonNode.class);

        SignedJWT signedJWT = mock(SignedJWT.class);
        String vcClaim = "{\"type\": [\"VerifiableCredential\", \"LEARCredentialEmployee\"]}";

        // Create and configure the simulated Payload
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("iss", "internal-auth-server");
        Payload jwtPayload = new Payload(payloadMap);

        when(signedJWT.getPayload()).thenReturn(jwtPayload);

        when(jwtService.parseJWT(token)).thenReturn(signedJWT);
        when(jwtService.getClaimFromPayload(jwtPayload, "vc")).thenReturn(vcClaim);

        // We use a real ObjectMapper to create the JsonNode we need
        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode vcJsonNode = realObjectMapper.readTree(vcClaim);
        when(objectMapper.readTree(vcClaim)).thenReturn(vcJsonNode);

        LEARCredentialEmployee learCredential = getLEARCredentialEmployee();
        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(vcClaim)).thenReturn(learCredential);

        when(authServerConfig.getJwtValidator()).thenReturn("internal-auth-server");

        // Act
        Mono<Void> result = policyAuthorizationService.authorize(token, LEAR_CREDENTIAL_EMPLOYEE, payload);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void authorize_failure_dueToInvalidCredentialType() throws Exception {
        // Arrange
        String token = "valid-token";
        JsonNode payload = mock(JsonNode.class);

        SignedJWT signedJWT = mock(SignedJWT.class);
        String vcClaim = "{\"type\": [\"VerifiableCredential\", \"InvalidCredentialType\"]}";

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("iss", "internal-auth-server");
        Payload jwtPayload = new Payload(payloadMap);

        when(signedJWT.getPayload()).thenReturn(jwtPayload);

        when(jwtService.parseJWT(token)).thenReturn(signedJWT);
        when(jwtService.getClaimFromPayload(jwtPayload, "vc")).thenReturn(vcClaim);

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode vcJsonNode = realObjectMapper.readTree(vcClaim);
        when(objectMapper.readTree(vcClaim)).thenReturn(vcJsonNode);

        // Act
        Mono<Void> result = policyAuthorizationService.authorize(token, LEAR_CREDENTIAL_EMPLOYEE, payload);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof InsufficientPermissionException &&
                                throwable.getMessage().contains("Unauthorized: Credential type 'LEARCredentialEmployee' is required."))
                .verify();
    }

    @Test
    void authorize_failure_dueToUnsupportedSchema() throws Exception {
        // Arrange
        String token = "valid-token";
        String schema = "UnsupportedSchema";
        JsonNode payload = mock(JsonNode.class);

        SignedJWT signedJWT = mock(SignedJWT.class);
        String vcClaim = "{\"type\": [\"VerifiableCredential\", \"LEARCredentialEmployee\"]}";

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("iss", "internal-auth-server");
        Payload jwtPayload = new Payload(payloadMap);

        when(signedJWT.getPayload()).thenReturn(jwtPayload);

        when(jwtService.parseJWT(token)).thenReturn(signedJWT);
        when(jwtService.getClaimFromPayload(jwtPayload, "vc")).thenReturn(vcClaim);

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode vcJsonNode = realObjectMapper.readTree(vcClaim);
        when(objectMapper.readTree(vcClaim)).thenReturn(vcJsonNode);

        LEARCredentialEmployee learCredential = getLEARCredentialEmployee();
        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(vcClaim)).thenReturn(learCredential);

        // Act
        Mono<Void> result = policyAuthorizationService.authorize(token, schema, payload);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof InsufficientPermissionException &&
                                throwable.getMessage().contains("Unauthorized: Unsupported schema"))
                .verify();
    }

    @Test
    void authorize_failure_dueToInvalidToken(){
        String token = "invalid-token";
        JsonNode payload = mock(JsonNode.class);

        when(jwtService.parseJWT(token)).thenThrow(new ParseErrorException("Invalid token"));

        // Act
        Mono<Void> result = policyAuthorizationService.authorize(token, LEAR_CREDENTIAL_EMPLOYEE, payload);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof ParseErrorException &&
                                throwable.getMessage().contains("Invalid token"))
                .verify();
    }

    @Test
    void authorize_failure_dueToIssuancePoliciesNotMet() throws Exception {
        // Arrange
        String token = "valid-token";
        JsonNode payload = mock(JsonNode.class);

        SignedJWT signedJWT = mock(SignedJWT.class);
        String vcClaim = "{\"type\": [\"VerifiableCredential\", \"LEARCredentialEmployee\"]}";

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("iss", "some-other-issuer");
        Payload jwtPayload = new Payload(payloadMap);

        when(signedJWT.getPayload()).thenReturn(jwtPayload);

        when(jwtService.parseJWT(token)).thenReturn(signedJWT);
        when(jwtService.getClaimFromPayload(jwtPayload, "vc")).thenReturn(vcClaim);

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode vcJsonNode = realObjectMapper.readTree(vcClaim);
        when(objectMapper.readTree(vcClaim)).thenReturn(vcJsonNode);

        LEARCredentialEmployee learCredential = getLEARCredentialEmployeeWithDifferentOrg();
        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(vcClaim)).thenReturn(learCredential);

        when(authServerConfig.getJwtValidator()).thenReturn("internal-auth-server");

        // Act
        Mono<Void> result = policyAuthorizationService.authorize(token, LEAR_CREDENTIAL_EMPLOYEE, payload);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof InsufficientPermissionException &&
                                throwable.getMessage().contains("Unauthorized: LEARCredentialEmployee does not meet any issuance policies."))
                .verify();
    }

    @Test
    void authorize_failure_dueToVerifiableCertificationPolicyNotMet() throws Exception {
        // Arrange
        String token = "valid-token";
        JsonNode payload = mock(JsonNode.class);

        SignedJWT signedJWT = mock(SignedJWT.class);
        String vcClaim = "{\"type\": [\"VerifiableCredential\", \"LEARCredentialEmployee\"]}";

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("iss", "some-other-issuer");
        Payload jwtPayload = new Payload(payloadMap);

        when(signedJWT.getPayload()).thenReturn(jwtPayload);

        when(jwtService.parseJWT(token)).thenReturn(signedJWT);
        when(jwtService.getClaimFromPayload(jwtPayload, "vc")).thenReturn(vcClaim);

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode vcJsonNode = realObjectMapper.readTree(vcClaim);
        when(objectMapper.readTree(vcClaim)).thenReturn(vcJsonNode);

        LEARCredentialEmployee learCredential = getLEARCredentialEmployee();
        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(vcClaim)).thenReturn(learCredential);

        // Act
        Mono<Void> result = policyAuthorizationService.authorize(token, VERIFIABLE_CERTIFICATION, payload);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof InsufficientPermissionException &&
                                throwable.getMessage().contains("Unauthorized: VerifiableCertification does not meet the issuance policy."))
                .verify();
    }

    @Test
    void authorize_success_withVerifiableCertification() throws Exception {
        // Arrange
        String token = "valid-token";
        JsonNode payload = mock(JsonNode.class);

        SignedJWT signedJWT = mock(SignedJWT.class);
        String vcClaim = "{\"type\": [\"VerifiableCredential\", \"LEARCredentialEmployee\"]}";

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("iss", "external-verifier");
        Payload jwtPayload = new Payload(payloadMap);

        when(signedJWT.getPayload()).thenReturn(jwtPayload);

        when(jwtService.parseJWT(token)).thenReturn(signedJWT);
        when(jwtService.getClaimFromPayload(jwtPayload, "vc")).thenReturn(vcClaim);

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode vcJsonNode = realObjectMapper.readTree(vcClaim);
        when(objectMapper.readTree(vcClaim)).thenReturn(vcJsonNode);

        LEARCredentialEmployee learCredential = getLEARCredentialEmployeeForCertification();
        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(vcClaim)).thenReturn(learCredential);

        when(verifierConfig.getVerifierExternalDomain()).thenReturn("external-verifier");

        // Act
        Mono<Void> result = policyAuthorizationService.authorize(token, VERIFIABLE_CERTIFICATION, payload);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void authorize_success_withMandatorIssuancePolicyValid() throws Exception {
        // Arrange
        String token = "valid-token";
        JsonNode payload = mock(JsonNode.class);

        LEARCredentialEmployee learCredential = getLEARCredentialEmployeeWithDifferentOrg();

        LEARCredentialEmployee.CredentialSubject.Mandate mandateFromPayload = LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                .mandator(LEARCredentialEmployee.CredentialSubject.Mandate.Mandator.builder()
                        .organizationIdentifier(learCredential.credentialSubject().mandate().mandator().organizationIdentifier())
                        .serialNumber(learCredential.credentialSubject().mandate().mandator().serialNumber())
                        .country(learCredential.credentialSubject().mandate().mandator().country())
                        .commonName(learCredential.credentialSubject().mandate().mandator().commonName())
                        .emailAddress(learCredential.credentialSubject().mandate().mandator().emailAddress())
                        .build())
                .power(Collections.singletonList(
                        LEARCredentialEmployee.CredentialSubject.Mandate.Power.builder()
                                .tmfFunction("ProductOffering")
                                .tmfAction(List.of("Create", "Update", "Delete"))
                                .build()))
                .build();
        when(objectMapper.convertValue(payload, LEARCredentialEmployee.CredentialSubject.Mandate.class)).thenReturn(mandateFromPayload);

        SignedJWT signedJWT = mock(SignedJWT.class);
        String vcClaim = "{\"type\": [\"VerifiableCredential\", \"LEARCredentialEmployee\"]}";

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("iss", "internal-auth-server");
        Payload jwtPayload = new Payload(payloadMap);

        when(signedJWT.getPayload()).thenReturn(jwtPayload);

        when(jwtService.parseJWT(token)).thenReturn(signedJWT);
        when(jwtService.getClaimFromPayload(jwtPayload, "vc")).thenReturn(vcClaim);

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode vcJsonNode = realObjectMapper.readTree(vcClaim);
        when(objectMapper.readTree(vcClaim)).thenReturn(vcJsonNode);

        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(vcClaim)).thenReturn(learCredential);
        when(authServerConfig.getJwtValidator()).thenReturn("internal-auth-server");

        // Act
        Mono<Void> result = policyAuthorizationService.authorize(token, LEAR_CREDENTIAL_EMPLOYEE, payload);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void authorize_failure_dueToInvalidPayloadPowers() throws Exception {
        // Arrange
        String token = "valid-token";
        JsonNode payload = mock(JsonNode.class);

        LEARCredentialEmployee learCredential = getLEARCredentialEmployeeWithDifferentOrg();

        LEARCredentialEmployee.CredentialSubject.Mandate mandateFromPayload = LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                .mandator(LEARCredentialEmployee.CredentialSubject.Mandate.Mandator.builder()
                        .organizationIdentifier(learCredential.credentialSubject().mandate().mandator().organizationIdentifier())
                        .serialNumber(learCredential.credentialSubject().mandate().mandator().serialNumber())
                        .country(learCredential.credentialSubject().mandate().mandator().country())
                        .commonName(learCredential.credentialSubject().mandate().mandator().commonName())
                        .emailAddress(learCredential.credentialSubject().mandate().mandator().emailAddress())
                        .build())
                .power(Collections.singletonList(
                        LEARCredentialEmployee.CredentialSubject.Mandate.Power.builder()
                                .tmfFunction("OtherFunction")
                                .tmfAction("SomeAction")
                                .build()))
                .build();
        when(objectMapper.convertValue(payload, LEARCredentialEmployee.CredentialSubject.Mandate.class)).thenReturn(mandateFromPayload);

        SignedJWT signedJWT = mock(SignedJWT.class);
        String vcClaim = "{\"type\": [\"VerifiableCredential\", \"LEARCredentialEmployee\"]}";

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("iss", "internal-auth-server");
        Payload jwtPayload = new Payload(payloadMap);

        when(signedJWT.getPayload()).thenReturn(jwtPayload);

        when(jwtService.parseJWT(token)).thenReturn(signedJWT);
        when(jwtService.getClaimFromPayload(jwtPayload, "vc")).thenReturn(vcClaim);

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode vcJsonNode = realObjectMapper.readTree(vcClaim);
        when(objectMapper.readTree(vcClaim)).thenReturn(vcJsonNode);

        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(vcClaim)).thenReturn(learCredential);
        when(authServerConfig.getJwtValidator()).thenReturn("internal-auth-server");

        // Act
        Mono<Void> result = policyAuthorizationService.authorize(token, LEAR_CREDENTIAL_EMPLOYEE, payload);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof InsufficientPermissionException &&
                                throwable.getMessage().contains("Unauthorized: LEARCredentialEmployee does not meet any issuance policies."))
                .verify();
    }

    @Test
    void authorize_failure_dueToTokenNotIssuedByInternalAuthServer() throws Exception {
        // Arrange
        String token = "valid-token";
        JsonNode payload = mock(JsonNode.class);

        SignedJWT signedJWT = mock(SignedJWT.class);
        String vcClaim = "{\"type\": [\"VerifiableCredential\", \"LEARCredentialEmployee\"]}";

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("iss", "some-other-issuer");
        Payload jwtPayload = new Payload(payloadMap);

        when(signedJWT.getPayload()).thenReturn(jwtPayload);

        when(jwtService.parseJWT(token)).thenReturn(signedJWT);
        when(jwtService.getClaimFromPayload(jwtPayload, "vc")).thenReturn(vcClaim);

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode vcJsonNode = realObjectMapper.readTree(vcClaim);
        when(objectMapper.readTree(vcClaim)).thenReturn(vcJsonNode);

        LEARCredentialEmployee learCredential = getLEARCredentialEmployee();
        when(learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(vcClaim)).thenReturn(learCredential);

        when(authServerConfig.getJwtValidator()).thenReturn("internal-auth-server");

        // Act
        Mono<Void> result = policyAuthorizationService.authorize(token, LEAR_CREDENTIAL_EMPLOYEE, payload);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof InsufficientPermissionException &&
                                throwable.getMessage().contains("Unauthorized: LEARCredentialEmployee does not meet any issuance policies."))
                .verify();
    }


    // Auxiliary methods to create LEARCredentialEmployee objects
    private LEARCredentialEmployee getLEARCredentialEmployee() {
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandator mandator = LEARCredentialEmployee.CredentialSubject.Mandate.Mandator.builder()
                .organizationIdentifier(IN2_ORGANIZATION_IDENTIFIER)
                .build();
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee = LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                .id("did:key:1234")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        LEARCredentialEmployee.CredentialSubject.Mandate.Power power = LEARCredentialEmployee.CredentialSubject.Mandate.Power.builder()
                .tmfFunction("Onboarding")
                .tmfAction("Execute")
                .build();
        LEARCredentialEmployee.CredentialSubject.Mandate mandate = LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                .mandator(mandator)
                .mandatee(mandatee)
                .power(Collections.singletonList(power))
                .build();
        LEARCredentialEmployee.CredentialSubject credentialSubject = LEARCredentialEmployee.CredentialSubject.builder()
                .mandate(mandate)
                .build();
        return LEARCredentialEmployee.builder()
                .type(List.of("VerifiableCredential", "LEARCredentialEmployee"))
                .credentialSubject(credentialSubject)
                .build();
    }

    private LEARCredentialEmployee getLEARCredentialEmployeeWithDifferentOrg() {
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandator mandator = LEARCredentialEmployee.CredentialSubject.Mandate.Mandator.builder()
                .organizationIdentifier("OTHER_ORGANIZATION")
                .commonName("SomeOtherOrganization")
                .country("ES")
                .emailAddress("someaddres@example.com")
                .serialNumber("123456")
                .build();
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee = LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                .id("did:key:1234")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        LEARCredentialEmployee.CredentialSubject.Mandate.Power power = LEARCredentialEmployee.CredentialSubject.Mandate.Power.builder()
                .tmfFunction("Onboarding")
                .tmfAction("Execute")
                .build();
        LEARCredentialEmployee.CredentialSubject.Mandate mandate = LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                .mandator(mandator)
                .mandatee(mandatee)
                .power(Collections.singletonList(power))
                .build();
        LEARCredentialEmployee.CredentialSubject credentialSubject = LEARCredentialEmployee.CredentialSubject.builder()
                .mandate(mandate)
                .build();
        return LEARCredentialEmployee.builder()
                .type(List.of("VerifiableCredential", "LEARCredentialEmployee"))
                .credentialSubject(credentialSubject)
                .build();
    }

    private LEARCredentialEmployee getLEARCredentialEmployeeForCertification() {
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandator mandator = LEARCredentialEmployee.CredentialSubject.Mandate.Mandator.builder()
                .organizationIdentifier("SomeOrganization")
                .build();
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee = LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                .id("did:key:1234")
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .build();
        LEARCredentialEmployee.CredentialSubject.Mandate.Power power = LEARCredentialEmployee.CredentialSubject.Mandate.Power.builder()
                .tmfFunction("Certification")
                .tmfAction("Attest")
                .build();
        LEARCredentialEmployee.CredentialSubject.Mandate mandate = LEARCredentialEmployee.CredentialSubject.Mandate.builder()
                .mandator(mandator)
                .mandatee(mandatee)
                .power(Collections.singletonList(power))
                .build();
        LEARCredentialEmployee.CredentialSubject credentialSubject = LEARCredentialEmployee.CredentialSubject.builder()
                .mandate(mandate)
                .build();
        return LEARCredentialEmployee.builder()
                .type(List.of("VerifiableCredential", "LEARCredentialEmployee"))
                .credentialSubject(credentialSubject)
                .build();
    }


}


