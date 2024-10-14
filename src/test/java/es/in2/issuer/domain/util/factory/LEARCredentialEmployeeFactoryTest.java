package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployee;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployeeJwtPayload;
import es.in2.issuer.domain.service.AccessTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LEARCredentialEmployeeFactoryTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AccessTokenService accessTokenService;

    @InjectMocks
    private LEARCredentialEmployeeFactory learCredentialEmployeeFactory;

    @Test
    void testMapCredentialAndBindMandateeIdInToTheCredential() throws JsonProcessingException {
        //Arrange
        String learCredential = "validCredentialString";
        String mandateeId = "mandateeId";
        String expectedString = "expectedString";
        LEARCredentialEmployeeJwtPayload learCredentialEmployeeJwtPayload = mock(LEARCredentialEmployeeJwtPayload.class);
        LEARCredentialEmployee learCredentialEmployee = mock(LEARCredentialEmployee.class);
        LEARCredentialEmployee.CredentialSubject credentialSubject = mock(LEARCredentialEmployee.CredentialSubject.class);
        LEARCredentialEmployee.CredentialSubject.Mandate mandate = mock(LEARCredentialEmployee.CredentialSubject.Mandate.class);
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandator mandator = mock(LEARCredentialEmployee.CredentialSubject.Mandate.Mandator.class);
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee = mock(LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.class);

        when(objectMapper.readValue(learCredential, LEARCredentialEmployeeJwtPayload.class)).thenReturn(learCredentialEmployeeJwtPayload);
        when(learCredentialEmployeeJwtPayload.learCredentialEmployee()).thenReturn(learCredentialEmployee);
        when(learCredentialEmployeeJwtPayload.learCredentialEmployee().credentialSubject()).thenReturn(credentialSubject);
        when(credentialSubject.mandate()).thenReturn(mandate);
        when(mandate.id()).thenReturn("mandateeId");
        when(mandate.mandator()).thenReturn(mandator);
        when(mandate.mandatee()).thenReturn(mandatee);
        when(mandatee.email()).thenReturn("email");
        when(mandatee.firstName()).thenReturn("firstName");
        when(mandatee.lastName()).thenReturn("lastName");
        when(mandatee.mobilePhone()).thenReturn("mobilePhone");
        when(mandate.power()).thenReturn(List.of(LEARCredentialEmployee.CredentialSubject.Mandate.Power.builder().build()));
        when(mandate.lifeSpan()).thenReturn(LEARCredentialEmployee.CredentialSubject.Mandate.LifeSpan.builder().build());
        when(learCredentialEmployeeJwtPayload.JwtId()).thenReturn("jwtId");
        when(learCredentialEmployeeJwtPayload.expirationTime()).thenReturn(0L);
        when(learCredentialEmployeeJwtPayload.issuedAt()).thenReturn(0L);
        when(learCredentialEmployeeJwtPayload.issuer()).thenReturn("issuer");
        when(learCredentialEmployeeJwtPayload.notValidBefore()).thenReturn(0L);
        when(objectMapper.writeValueAsString(any(LEARCredentialEmployeeJwtPayload.class))).thenReturn(expectedString);

        //Act & Assert
        StepVerifier.create(learCredentialEmployeeFactory.mapCredentialAndBindMandateeIdInToTheCredential(learCredential, mandateeId))
                .expectNext(expectedString)
                .verifyComplete();
    }

    @Test
    void testMapAndBuildLEARCredentialEmployee() throws JsonProcessingException {
        //Arrange
        String json = "{\"test\": \"test\"}";
        JsonNode jsonNode = objectMapper.readTree(json);
        LEARCredentialEmployee.CredentialSubject.Mandate mockMandate = mock(LEARCredentialEmployee.CredentialSubject.Mandate.class);
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandator mockMandator = mock(LEARCredentialEmployee.CredentialSubject.Mandate.Mandator.class);
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mockMandatee = mock(LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.class);
        LEARCredentialEmployee.CredentialSubject.Mandate.Power mockPower = mock(LEARCredentialEmployee.CredentialSubject.Mandate.Power.class);
        LEARCredentialEmployee.CredentialSubject.Mandate.Signer mockSigner = mock(LEARCredentialEmployee.CredentialSubject.Mandate.Signer.class);

        List<LEARCredentialEmployee.CredentialSubject.Mandate.Power> mockPowerList = new ArrayList<>();
        mockPowerList.add(mockPower);

        when(objectMapper.convertValue(jsonNode, LEARCredentialEmployee.CredentialSubject.Mandate.class))
                .thenReturn(mockMandate);
        when(mockMandate.mandator()).thenReturn(mockMandator);
        when(mockMandator.organizationIdentifier()).thenReturn("orgId");
        when(mockMandate.mandatee()).thenReturn(mockMandatee);
        when(mockMandate.power()).thenReturn(mockPowerList);
        when(mockMandatee.id()).thenReturn("mandateeId");
        when(mockMandate.signer()).thenReturn(mockSigner);
        when(mockMandate.signer().organizationIdentifier()).thenReturn("signerOrgId");
        when(objectMapper.writeValueAsString(any(LEARCredentialEmployeeJwtPayload.class))).thenReturn(json);
        when(accessTokenService.getOrganizationIdFromCurrentSession()).thenReturn(Mono.just("orgId"));

        // Act
        Mono<CredentialProcedureCreationRequest> result = learCredentialEmployeeFactory.mapAndBuildLEARCredentialEmployee(jsonNode);

        //Assert
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

}