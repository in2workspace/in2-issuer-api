package es.in2.issuer.infrastructure.config.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployee;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.infrastructure.config.security.service.impl.PolicyAuthorizationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AuthorizationServiceException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL_EMPLOYEE;
import static es.in2.issuer.domain.util.Constants.VERIFIABLE_CERTIFICATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyAuthorizationServiceImplTest {

    @Mock
    private JWTService jwtService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PolicyAuthorizationServiceImpl policyAuthorizationService;

    private JsonNode learCredentialEmployeeJwt;

    private JsonNode nonLearCredentialEmployeeJwt;

    @BeforeEach
    void setUp() {
        ObjectMapper testObjectMapper = new ObjectMapper();

        // Create JsonNode representing a LEARCredentialEmployee credential
        ObjectNode learCredentialEmployeeJwtNode = testObjectMapper.createObjectNode();
        ArrayNode typeArrayNode = testObjectMapper.createArrayNode();
        typeArrayNode.add("VerifiableCredential");
        typeArrayNode.add("LEARCredentialEmployee");
        learCredentialEmployeeJwtNode.set("type", typeArrayNode);
        learCredentialEmployeeJwt = learCredentialEmployeeJwtNode;

        // Create JsonNode representing a non-LEARCredentialEmployee credential
        ObjectNode nonLearCredentialEmployeeJwtNode = testObjectMapper.createObjectNode();
        ArrayNode nonTypeArrayNode = testObjectMapper.createArrayNode();
        nonTypeArrayNode.add("VerifiableCredential");
        nonTypeArrayNode.add("OtherCredentialType");
        nonLearCredentialEmployeeJwtNode.set("type", nonTypeArrayNode);
        nonLearCredentialEmployeeJwt = nonLearCredentialEmployeeJwtNode;
    }

    @Test
    void authorize_whenCredentialIsLEARCredentialEmployee_andSchemaIsLEARCredentialEmployee_shouldAuthorize() {
        // Arrange
        String authorizationHeader = "Bearer some.jwt.token";

        when(jwtService.parseJwtVCAsJsonNode(authorizationHeader)).thenReturn(Mono.just(learCredentialEmployeeJwt));

        // Act & Assert
        StepVerifier.create(policyAuthorizationService.authorize(authorizationHeader, LEAR_CREDENTIAL_EMPLOYEE))
                .verifyComplete();

        // Verify that no further processing was done
        verifyNoInteractions(objectMapper);
    }

    @Test
    void authorize_whenCredentialIsLEARCredentialEmployee_andSchemaIsVerifiableCertification_andHasProperPowers_shouldAuthorize() throws Exception {
        // Arrange
        String authorizationHeader = "Bearer some.jwt.token";

        when(jwtService.parseJwtVCAsJsonNode(authorizationHeader)).thenReturn(Mono.just(learCredentialEmployeeJwt));

        LEARCredentialEmployee credential = createValidLearCredentialEmployee();

        when(objectMapper.treeToValue(eq(learCredentialEmployeeJwt), eq(LEARCredentialEmployee.class)))
                .thenReturn(credential);

        // Act & Assert
        StepVerifier.create(policyAuthorizationService.authorize(authorizationHeader, VERIFIABLE_CERTIFICATION))
                .verifyComplete();
    }

    @Test
    void authorize_whenCredentialIsLEARCredentialEmployee_andSchemaIsVerifiableCertification_andDoesNotHaveProperPowers_shouldThrowAuthorizationException() throws Exception {
        // Arrange
        String authorizationHeader = "Bearer some.jwt.token";

        when(jwtService.parseJwtVCAsJsonNode(authorizationHeader)).thenReturn(Mono.just(learCredentialEmployeeJwt));

        LEARCredentialEmployee credential = createInvalidLearCredentialEmployee();

        when(objectMapper.treeToValue(eq(learCredentialEmployeeJwt), eq(LEARCredentialEmployee.class)))
                .thenReturn(credential);

        // Act & Assert
        StepVerifier.create(policyAuthorizationService.authorize(authorizationHeader, VERIFIABLE_CERTIFICATION))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof AuthorizationServiceException);
                    assertEquals("Unauthorized: The credential does not meet the power requirements.", error.getMessage());
                })
                .verify();
    }

    @Test
    void authorize_whenCredentialIsNotLEARCredentialEmployee_shouldThrowAuthorizationException() {
        // Arrange
        String authorizationHeader = "Bearer some.jwt.token";

        when(jwtService.parseJwtVCAsJsonNode(authorizationHeader)).thenReturn(Mono.just(nonLearCredentialEmployeeJwt));

        // Act & Assert
        StepVerifier.create(policyAuthorizationService.authorize(authorizationHeader, LEAR_CREDENTIAL_EMPLOYEE))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof AuthorizationServiceException);
                    assertEquals("Unauthorized: Your credential does not match the requirements.", error.getMessage());
                })
                .verify();
    }

    @Test
    void authorize_whenExceptionOccursDuringProcessing_shouldThrowAuthorizationException() throws Exception {
        // Arrange
        String authorizationHeader = "Bearer some.jwt.token";

        when(jwtService.parseJwtVCAsJsonNode(authorizationHeader)).thenReturn(Mono.just(learCredentialEmployeeJwt));

        when(objectMapper.treeToValue(eq(learCredentialEmployeeJwt), eq(LEARCredentialEmployee.class)))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // Act & Assert
        StepVerifier.create(policyAuthorizationService.authorize(authorizationHeader, VERIFIABLE_CERTIFICATION))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof AuthorizationServiceException);
                    assertTrue(error.getMessage().contains("Error processing the credential: "));
                    assertTrue(error.getCause() instanceof JsonProcessingException);
                })
                .verify();
    }

    @Test
    void authorize_whenJwtServiceReturnsError_shouldPropagateError() {
        // Arrange
        String authorizationHeader = "Bearer some.jwt.token";

        when(jwtService.parseJwtVCAsJsonNode(authorizationHeader))
                .thenReturn(Mono.error(new RuntimeException("JWT parsing error")));

        // Act & Assert
        StepVerifier.create(policyAuthorizationService.authorize(authorizationHeader, LEAR_CREDENTIAL_EMPLOYEE))
                .expectErrorSatisfies(error -> {
                    assertTrue(error instanceof RuntimeException);
                    assertEquals("JWT parsing error", error.getMessage());
                })
                .verify();
    }

    // Helper methods to create LEARCredentialEmployee instances

    private LEARCredentialEmployee createValidLearCredentialEmployee() {
        LEARCredentialEmployee.CredentialSubject.Mandate.Power power = new LEARCredentialEmployee.CredentialSubject.Mandate.Power(
                "123",
                List.of("Attest", "OtherAction"),
                "Dome",
                "Certification",
                ""

        );

        LEARCredentialEmployee.CredentialSubject.Mandate mandate = new LEARCredentialEmployee.CredentialSubject.Mandate(
                "1234",
                null,
                null,
                null,
                List.of(power),
                null
        );

        LEARCredentialEmployee.CredentialSubject credentialSubject = new LEARCredentialEmployee.CredentialSubject(
                mandate
        );

        return new LEARCredentialEmployee(null, null, null, credentialSubject, null, null, null);
    }

    private LEARCredentialEmployee createInvalidLearCredentialEmployee() {
        LEARCredentialEmployee.CredentialSubject.Mandate.Power power = new LEARCredentialEmployee.CredentialSubject.Mandate.Power(
                "123",
                List.of("InvalidAction"),
                "Dome",
                "NotCertification",
                ""

        );

        LEARCredentialEmployee.CredentialSubject.Mandate mandate = new LEARCredentialEmployee.CredentialSubject.Mandate(
                "1234",
                null,
                null,
                null,
                List.of(power),
                null
        );

        LEARCredentialEmployee.CredentialSubject credentialSubject = new LEARCredentialEmployee.CredentialSubject(
                mandate
        );

        return new LEARCredentialEmployee(null, null, null, credentialSubject, null, null, null);
    }
}

