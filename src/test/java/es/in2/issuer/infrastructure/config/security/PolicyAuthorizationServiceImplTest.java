package es.in2.issuer.infrastructure.config.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.Payload;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.exception.InsufficientPermissionException;
import es.in2.issuer.domain.exception.ParseErrorException;
import es.in2.issuer.domain.model.dto.credential.lear.Mandator;
import es.in2.issuer.domain.model.dto.credential.lear.Power;
import es.in2.issuer.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.domain.model.dto.credential.lear.machine.LEARCredentialMachine;
import es.in2.issuer.domain.service.CredentialProcedureService;
import es.in2.issuer.domain.service.DeferredCredentialMetadataService;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.domain.util.factory.CredentialFactory;
import es.in2.issuer.domain.util.factory.LEARCredentialEmployeeFactory;
import es.in2.issuer.domain.util.factory.LEARCredentialMachineFactory;
import es.in2.issuer.domain.util.factory.VerifiableCertificationFactory;
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
    private LEARCredentialEmployeeFactory learCredentialEmployeeFactory;
    @Mock
    private VerifiableCertificationFactory verifiableCertificationFactory;
    @Mock
    private LEARCredentialMachineFactory learCredentialMachineFactory;
    @Mock
    private CredentialProcedureService credentialProcedureService;
    @Mock
    private DeferredCredentialMetadataService deferredCredentialMetadataService;

    @InjectMocks
    private PolicyAuthorizationServiceImpl policyAuthorizationService;

    @BeforeEach
    void setUp() {
        // Creamos una instancia real de CredentialFactory, pasando los mocks necesarios
        CredentialFactory credentialFactory = new CredentialFactory(learCredentialEmployeeFactory, learCredentialMachineFactory, verifiableCertificationFactory, credentialProcedureService, deferredCredentialMetadataService);

        // Inicializamos policyAuthorizationService con las dependencias adecuadas
        policyAuthorizationService = new PolicyAuthorizationServiceImpl(
                jwtService,
                objectMapper,
                credentialFactory
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
                                throwable.getMessage().contains("Unauthorized: Credential type 'LEARCredentialEmployee' or 'LEARCredentialMachine' is required."))
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
                .mandator(Mandator.builder()
                        .organizationIdentifier(learCredential.credentialSubject().mandate().mandator().organizationIdentifier())
                        .serialNumber(learCredential.credentialSubject().mandate().mandator().serialNumber())
                        .country(learCredential.credentialSubject().mandate().mandator().country())
                        .commonName(learCredential.credentialSubject().mandate().mandator().commonName())
                        .emailAddress(learCredential.credentialSubject().mandate().mandator().emailAddress())
                        .build())
                .power(Collections.singletonList(
                        Power.builder()
                                .function("ProductOffering")
                                .action(List.of("Create", "Update", "Delete"))
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
                .mandator(Mandator.builder()
                        .organizationIdentifier(learCredential.credentialSubject().mandate().mandator().organizationIdentifier())
                        .serialNumber(learCredential.credentialSubject().mandate().mandator().serialNumber())
                        .country(learCredential.credentialSubject().mandate().mandator().country())
                        .commonName(learCredential.credentialSubject().mandate().mandator().commonName())
                        .emailAddress(learCredential.credentialSubject().mandate().mandator().emailAddress())
                        .build())
                .power(Collections.singletonList(
                        Power.builder()
                                .function("OtherFunction")
                                .action("SomeAction")
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
    void authorize_success_withLearCredentialMachine() throws Exception {
        // Arrange
        String token = "valid-token";
        JsonNode payload = mock(JsonNode.class);
        // El vcClaim indica que se trata de una credencial de máquina
        String vcClaim = "{\"type\": [\"VerifiableCredential\", \"LEARCredentialMachine\"]}";

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("iss", "internal-auth-server");
        Payload jwtPayload = new Payload(payloadMap);

        SignedJWT signedJWT = mock(SignedJWT.class);
        when(signedJWT.getPayload()).thenReturn(jwtPayload);

        when(jwtService.parseJWT(token)).thenReturn(signedJWT);
        when(jwtService.getClaimFromPayload(jwtPayload, "vc")).thenReturn(vcClaim);

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode vcJsonNode = realObjectMapper.readTree(vcClaim);
        when(objectMapper.readTree(vcClaim)).thenReturn(vcJsonNode);

        // Emulate that the machine factory returns a credential that meets the policy
        LEARCredentialMachine machineCredential = getLEARCredentialMachine();
        when(learCredentialMachineFactory.mapStringToLEARCredentialMachine(vcClaim)).thenReturn(machineCredential);

        // Act
        Mono<Void> result = policyAuthorizationService.authorize(token, LEAR_CREDENTIAL_EMPLOYEE, payload);

        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    void authorize_failure_withLearCredentialMachine_dueToPolicy() throws Exception {
        // Arrange
        String token = "valid-token";
        JsonNode payload = mock(JsonNode.class);
        String vcClaim = "{\"type\": [\"VerifiableCredential\", \"LEARCredentialMachine\"]}";

        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("iss", "internal-auth-server");
        Payload jwtPayload = new Payload(payloadMap);

        SignedJWT signedJWT = mock(SignedJWT.class);
        when(signedJWT.getPayload()).thenReturn(jwtPayload);

        when(jwtService.parseJWT(token)).thenReturn(signedJWT);
        when(jwtService.getClaimFromPayload(jwtPayload, "vc")).thenReturn(vcClaim);

        ObjectMapper realObjectMapper = new ObjectMapper();
        JsonNode vcJsonNode = realObjectMapper.readTree(vcClaim);
        when(objectMapper.readTree(vcClaim)).thenReturn(vcJsonNode);

        // We emulate that the machine factory returns a credential that does NOT meet the policy
        LEARCredentialMachine machineCredential = getLEARCredentialMachineWithInvalidPolicy();
        when(learCredentialMachineFactory.mapStringToLEARCredentialMachine(vcClaim)).thenReturn(machineCredential);

        // Act
        Mono<Void> result = policyAuthorizationService.authorize(token, LEAR_CREDENTIAL_EMPLOYEE, payload);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof InsufficientPermissionException &&
                                throwable.getMessage().contains("Unauthorized: LEARCredentialEmployee does not meet any issuance policies."))
                .verify();
    }

    // Auxiliary methods to create LEARCredentialEmployee objects
    private LEARCredentialEmployee getLEARCredentialEmployee() {
        Mandator mandator = Mandator.builder()
                .organizationIdentifier(IN2_ORGANIZATION_IDENTIFIER)
                .build();
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee = LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                .id("did:key:1234")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        Power power = Power.builder()
                .function("Onboarding")
                .action("Execute")
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
        Mandator mandator = Mandator.builder()
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
        Power power = Power.builder()
                .function("Onboarding")
                .action("Execute")
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
        Mandator mandator = Mandator.builder()
                .organizationIdentifier("SomeOrganization")
                .build();
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee = LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.builder()
                .id("did:key:1234")
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .build();
        Power power = Power.builder()
                .function("Certification")
                .action("Attest")
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

    private LEARCredentialMachine getLEARCredentialMachine() {
        Mandator mandator = Mandator.builder()
                .organizationIdentifier(IN2_ORGANIZATION_IDENTIFIER)
                .build();
        LEARCredentialMachine.CredentialSubject.Mandate.Mandatee mandatee = LEARCredentialMachine.CredentialSubject.Mandate.Mandatee.builder()
                .id("did:key:1234")
                .build();
        Power power = Power.builder()
                .function("Onboarding")
                .action("Execute")
                .build();
        LEARCredentialMachine.CredentialSubject.Mandate mandate = LEARCredentialMachine.CredentialSubject.Mandate.builder()
                .mandator(mandator)
                .mandatee(mandatee)
                .power(Collections.singletonList(power))
                .build();
        LEARCredentialMachine.CredentialSubject credentialSubject = LEARCredentialMachine.CredentialSubject.builder()
                .mandate(mandate)
                .build();
        return LEARCredentialMachine.builder()
                .type(List.of("VerifiableCredential", "LEARCredentialMachine"))
                .credentialSubject(credentialSubject)
                .build();
    }

    private LEARCredentialMachine getLEARCredentialMachineWithInvalidPolicy() {
        Mandator mandator = Mandator.builder()
                .organizationIdentifier(IN2_ORGANIZATION_IDENTIFIER)
                .build();
        LEARCredentialMachine.CredentialSubject.Mandate.Mandatee mandatee =
                LEARCredentialMachine.CredentialSubject.Mandate.Mandatee.builder()
                        .id("did:key:1234")
                        .build();
        // Create an empty list of powers to simulate that the policy is not met
        List<Power> emptyPowers = Collections.emptyList();
        LEARCredentialMachine.CredentialSubject.Mandate mandate =
                LEARCredentialMachine.CredentialSubject.Mandate.builder()
                        .mandator(mandator)
                        .mandatee(mandatee)
                        .power(emptyPowers)
                        .build();
        LEARCredentialMachine.CredentialSubject credentialSubject =
                LEARCredentialMachine.CredentialSubject.builder()
                        .mandate(mandate)
                        .build();
        return LEARCredentialMachine.builder()
                .type(List.of("VerifiableCredential", "LEARCredentialMachine"))
                .credentialSubject(credentialSubject)
                .build();
    }
}


