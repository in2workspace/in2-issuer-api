package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.InvalidCredentialFormatException;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployeeJwtPayload;
import es.in2.issuer.domain.model.dto.credential.lear.Mandator;
import es.in2.issuer.domain.model.dto.credential.lear.Power;
import es.in2.issuer.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.domain.service.AccessTokenService;
import es.in2.issuer.infrastructure.config.DefaultSignerConfig;
import es.in2.issuer.infrastructure.config.RemoteSignatureConfig;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LEARCredentialEmployeeFactoryTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private RemoteSignatureConfig remoteSignatureConfig;

    @InjectMocks
    private LEARCredentialEmployeeFactory learCredentialEmployeeFactory;

    @Mock
    private DefaultSignerConfig defaultSignerConfig;

    @Test
    void testMapCredentialAndBindMandateeIdInToTheCredential() throws JsonProcessingException, InvalidCredentialFormatException {
        //Arrange
        String learCredential = "validCredentialString";
        String mandateeId = "mandateeId";
        String expectedString = "expectedString";
        LEARCredentialEmployeeJwtPayload learCredentialEmployeeJwtPayload = mock(LEARCredentialEmployeeJwtPayload.class);
        LEARCredentialEmployee learCredentialEmployee = mock(LEARCredentialEmployee.class);
        LEARCredentialEmployee.CredentialSubject credentialSubject = mock(LEARCredentialEmployee.CredentialSubject.class);
        LEARCredentialEmployee.CredentialSubject.Mandate mandate = mock(LEARCredentialEmployee.CredentialSubject.Mandate.class);
        Mandator mandator = mock(Mandator.class);
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mandatee = mock(LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.class);

        when(objectMapper.readValue(learCredential, LEARCredentialEmployee.class)).thenReturn(learCredentialEmployee);
        when(learCredentialEmployeeJwtPayload.learCredentialEmployee()).thenReturn(learCredentialEmployee);
        when(learCredentialEmployeeJwtPayload.learCredentialEmployee().credentialSubject()).thenReturn(credentialSubject);
        when(credentialSubject.mandate()).thenReturn(mandate);
        when(mandate.id()).thenReturn("mandateeId");
        when(mandate.mandator()).thenReturn(mandator);
        when(mandate.mandatee()).thenReturn(mandatee);
        when(mandatee.email()).thenReturn("email");
        when(mandatee.firstName()).thenReturn("firstName");
        when(mandatee.lastName()).thenReturn("lastName");
        when(mandatee.nationality()).thenReturn("nationality");
        when(mandate.power()).thenReturn(List.of(Power.builder().build()));
        when(objectMapper.writeValueAsString(any(LEARCredentialEmployee.class))).thenReturn(expectedString);

        //Act & Assert
        StepVerifier.create(learCredentialEmployeeFactory.mapCredentialAndBindMandateeIdInToTheCredential(learCredential, mandateeId))
                .expectNext(expectedString)
                .verifyComplete();
    }

    @Test
    void testMapCredentialAndBindIssuerInToTheCredential() throws JsonProcessingException, InvalidCredentialFormatException {
        // Arrange
        String learCredential = "validCredentialString";
        String procedureId = "procedureId";
        String expectedString = "expectedString";

        LEARCredentialEmployee learCredentialEmployee = mock(LEARCredentialEmployee.class);
        LEARCredentialEmployee.CredentialSubject credentialSubject = mock(LEARCredentialEmployee.CredentialSubject.class);

        // Mock the initial parsing
        when(objectMapper.readValue(learCredential, LEARCredentialEmployee.class)).thenReturn(learCredentialEmployee);

        // Mock the internal structure
        when(learCredentialEmployee.credentialSubject()).thenReturn(credentialSubject);

        // Mock conversion to string
        when(objectMapper.writeValueAsString(any(LEARCredentialEmployee.class))).thenReturn(expectedString);
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn("server");
        // Act & Assert
        StepVerifier.create(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(learCredential, procedureId))
                .expectNext(expectedString)
                .verifyComplete();

        // Verify interactions
        verify(objectMapper).readValue(learCredential, LEARCredentialEmployee.class);
        verify(objectMapper).writeValueAsString(any(LEARCredentialEmployee.class));
    }

    @Test
    void testMapAndBuildLEARCredentialEmployee() throws JsonProcessingException {
        //Arrange
        String json = "{\"test\": \"test\"}";
        JsonNode jsonNode = objectMapper.readTree(json);
        LEARCredentialEmployee.CredentialSubject.Mandate mockMandate = mock(LEARCredentialEmployee.CredentialSubject.Mandate.class);
        Mandator mockMandator = mock(Mandator.class);
        LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee mockMandatee = mock(LEARCredentialEmployee.CredentialSubject.Mandate.Mandatee.class);
        Power mockPower = mock(Power.class);

        List<Power> mockPowerList = new ArrayList<>();
        mockPowerList.add(mockPower);

        when(objectMapper.convertValue(jsonNode, LEARCredentialEmployee.CredentialSubject.Mandate.class))
                .thenReturn(mockMandate);
        when(mockMandate.mandator()).thenReturn(mockMandator);
        when(mockMandate.mandatee()).thenReturn(mockMandatee);
        when(mockMandate.power()).thenReturn(mockPowerList);

        when(objectMapper.writeValueAsString(any(LEARCredentialEmployee.class))).thenReturn(json);
        when(accessTokenService.getOrganizationIdFromCurrentSession()).thenReturn(Mono.just("orgId"));

        // Act
        Mono<CredentialProcedureCreationRequest> result = learCredentialEmployeeFactory.mapAndBuildLEARCredentialEmployee(jsonNode, "S");

        //Assert
        StepVerifier.create(result)
                .expectNextCount(1)
                .verifyComplete();
    }

}