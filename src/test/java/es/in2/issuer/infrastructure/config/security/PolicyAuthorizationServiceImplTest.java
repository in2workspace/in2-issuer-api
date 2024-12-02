package es.in2.issuer.infrastructure.config.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.SignedJWT;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployee;
import es.in2.issuer.domain.service.JWTService;
import es.in2.issuer.domain.util.factory.CredentialFactory;
import es.in2.issuer.infrastructure.config.AuthServerConfig;
import es.in2.issuer.infrastructure.config.VerifierConfig;
import es.in2.issuer.infrastructure.config.security.service.impl.PolicyAuthorizationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static es.in2.issuer.domain.util.Constants.IN2_ORGANIZATION_IDENTIFIER;
import static es.in2.issuer.domain.util.Constants.LEAR_CREDENTIAL_EMPLOYEE;
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
    private CredentialFactory credentialFactory;

    @Mock
    private VerifierConfig verifierConfig;

    @InjectMocks
    private PolicyAuthorizationServiceImpl policyAuthorizationService;



    // Test methods will go here
    @Test
    void testAuthorize_Success_LEAR_CREDENTIAL_EMPLOYEE() throws Exception {
        // Arrange
        String authorizationHeader = "Bearer validToken";
        JsonNode payload = mock(JsonNode.class);

        String token = "validToken";
        SignedJWT signedJWT = mock(SignedJWT.class);
        String vcClaim = "{\"type\":\"LEARCredentialEmployee\"}";

        LEARCredentialEmployee learCredential = mock(LEARCredentialEmployee.class);
        LEARCredentialEmployee.CredentialSubject credentialSubject = mock(LEARCredentialEmployee.CredentialSubject.class);
        LEARCredentialEmployee.CredentialSubject.Mandate mandate = mock(LEARCredentialEmployee.CredentialSubject.Mandate.class);
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandator mandator = mock(LEARCredentialEmployee.CredentialSubject.Mandate.Mandator.class);

        when(jwtService.parseJWT(token)).thenReturn(signedJWT);
        when(jwtService.getClaimFromPayload(signedJWT.getPayload(), "vc")).thenReturn(vcClaim);
        when(credentialFactory.learCredentialEmployeeFactory.mapStringToLEARCredentialEmployee(vcClaim)).thenReturn(learCredential);

        when(learCredential.credentialSubject()).thenReturn(credentialSubject);
        when(credentialSubject.mandate()).thenReturn(mandate);
        when(mandate.mandator()).thenReturn(mandator);
        when(mandator.organizationIdentifier()).thenReturn(IN2_ORGANIZATION_IDENTIFIER);

        when(mandate.power()).thenReturn(List.of(
                new LEARCredentialEmployee.CredentialSubject.Mandate.Power("123","Execute", "Example","Onboarding", "Domain")
        ));

        when(authServerConfig.getJwtValidator()).thenReturn("issuer");

        when(signedJWT.getPayload()).thenReturn(signedJWT.getPayload());
        when(signedJWT.getJWTClaimsSet()).thenReturn(mock(com.nimbusds.jwt.JWTClaimsSet.class));
        when(signedJWT.getJWTClaimsSet().getClaim("iss")).thenReturn("issuer");

        // Act
        Mono<Void> result = policyAuthorizationService.authorize(authorizationHeader, LEAR_CREDENTIAL_EMPLOYEE, payload);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

}


