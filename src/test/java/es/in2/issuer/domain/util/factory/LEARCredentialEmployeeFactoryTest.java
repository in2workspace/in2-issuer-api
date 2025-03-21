package es.in2.issuer.domain.util.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.exception.InvalidCredentialFormatException;
import es.in2.issuer.domain.exception.RemoteSignatureException;
import es.in2.issuer.domain.model.dto.CredentialProcedureCreationRequest;
import es.in2.issuer.domain.model.dto.LEARCredentialEmployeeJwtPayload;
import es.in2.issuer.domain.model.dto.credential.DetailedIssuer;
import es.in2.issuer.domain.model.dto.credential.lear.Mandator;
import es.in2.issuer.domain.model.dto.credential.lear.Power;
import es.in2.issuer.domain.model.dto.credential.lear.employee.LEARCredentialEmployee;
import es.in2.issuer.domain.service.AccessTokenService;
import es.in2.issuer.domain.service.impl.RemoteSignatureServiceImpl;
import es.in2.issuer.infrastructure.config.DefaultSignerConfig;
import es.in2.issuer.infrastructure.config.RemoteSignatureConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.ConnectException;
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

    @Mock
    private RemoteSignatureServiceImpl remoteSignatureServiceImpl;

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

    @Test
    void mapCredentialAndBindIssuerInToTheCredential_Server_Success() throws JsonProcessingException, InvalidCredentialFormatException {
        String procedureId = "procedureId";
        String credentialString = "validCredentialString";
        String expectedString = "expectedString";

        LEARCredentialEmployee learCredentialEmployee = mock(LEARCredentialEmployee.class);

        when(objectMapper.readValue(credentialString, LEARCredentialEmployee.class)).thenReturn(learCredentialEmployee);
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn("server");
        when(defaultSignerConfig.getOrganizationIdentifier()).thenReturn("ORG123");
        when(defaultSignerConfig.getOrganization()).thenReturn("Company");
        when(defaultSignerConfig.getCountry()).thenReturn("ES");
        when(defaultSignerConfig.getCommonName()).thenReturn("Signer CN");
        when(defaultSignerConfig.getEmail()).thenReturn("signer@email.com");
        when(defaultSignerConfig.getSerialNumber()).thenReturn("123456789");
        when(objectMapper.writeValueAsString(any(LEARCredentialEmployee.class))).thenReturn(expectedString);

        StepVerifier.create(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(credentialString, procedureId))
                .expectNext(expectedString)
                .verifyComplete();

        verify(remoteSignatureServiceImpl, never()).validateCredentials();
    }

    @Disabled
    @Test
    void mapCredentialAndBindIssuerInToTheCredential_InvalidCredentials_Error() throws JsonProcessingException, InvalidCredentialFormatException {
        String procedureId = "550e8400-e29b-41d4-a716-446655440000";
        String credentialString = "validCredentialString";

        LEARCredentialEmployee learCredentialEmployee = mock(LEARCredentialEmployee.class);

        when(objectMapper.readValue(credentialString, LEARCredentialEmployee.class)).thenReturn(learCredentialEmployee);
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn("cloud");
        when(remoteSignatureServiceImpl.validateCredentials()).thenReturn(Mono.just(false));

        when(remoteSignatureServiceImpl.handlePostRecoverError(any(), eq(procedureId))).thenReturn(Mono.empty());

        StepVerifier.create(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(credentialString, procedureId))
                .expectError(RemoteSignatureException.class)
                .verify();

        verify(remoteSignatureServiceImpl).validateCredentials();
    }
    @Disabled
    @Test
    void mapCredentialAndBindIssuerInToTheCredential_ValidateCredentials_FailsAfterRetries_SwitchToAsync() throws JsonProcessingException, InvalidCredentialFormatException {
        String procedureId = "550e8400-e29b-41d4-a716-446655440000";
        String credentialString = "validCredentialString";

        LEARCredentialEmployee learCredentialEmployee = mock(LEARCredentialEmployee.class);

        when(objectMapper.readValue(credentialString, LEARCredentialEmployee.class)).thenReturn(learCredentialEmployee);
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn("cloud");

        when(remoteSignatureServiceImpl.validateCredentials())
                .thenAnswer(invocation -> Mono.error(new ConnectException("Connection timeout")));

        when(remoteSignatureServiceImpl.isRecoverableError(any())).thenReturn(true);
        when(remoteSignatureServiceImpl.handlePostRecoverError(any(), eq(procedureId))).thenReturn(Mono.empty());

        StepVerifier.create(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(credentialString, procedureId))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(RemoteSignatureException.class, throwable);
                    Assertions.assertEquals("Signature Failed, changed to ASYNC mode", throwable.getMessage());
                })
                .verify();

        verify(remoteSignatureServiceImpl, times(4)).validateCredentials();
        verify(remoteSignatureServiceImpl).handlePostRecoverError(any(), eq(procedureId));
    }
    @Disabled
    @Test
    void mapCredentialAndBindIssuerInToTheCredential_ValidateCredentials_SuccessOnSecondAttempt() throws JsonProcessingException, InvalidCredentialFormatException {
        String procedureId = "550e8400-e29b-41d4-a716-446655440000";
        String credentialString = "validCredentialString";
        String expectedString = "expectedString";


        LEARCredentialEmployee learCredentialEmployee = mock(LEARCredentialEmployee.class);
        DetailedIssuer issuer = mock(DetailedIssuer.class);


        when(objectMapper.readValue(credentialString, LEARCredentialEmployee.class)).thenReturn(learCredentialEmployee);
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn("cloud");

        when(remoteSignatureServiceImpl.validateCredentials())
                .thenReturn(Mono.error(new ConnectException("Temporary failure")))
                .thenReturn(Mono.just(true));

        when(remoteSignatureServiceImpl.isRecoverableError(any())).thenReturn(true);
        when(remoteSignatureServiceImpl.requestAccessToken(any(), eq("service"))).thenReturn(Mono.just("validToken"));
        when(remoteSignatureServiceImpl.requestCertificateInfo(eq("validToken"), any())).thenReturn(Mono.just("mockedCertificateInfo"));
        when(remoteSignatureServiceImpl.extractIssuerFromCertificateInfo(any())).thenReturn(Mono.just(issuer));
        when(objectMapper.writeValueAsString(any(LEARCredentialEmployee.class))).thenReturn(expectedString);

        StepVerifier.create(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(credentialString, procedureId))
                .expectNext(expectedString)
                .verifyComplete();

        verify(remoteSignatureServiceImpl, times(2)).validateCredentials();
    }
    @Disabled
    @Test
    void mapCredentialAndBindIssuerInToTheCredential_ValidateCredentials_NonRecoverableError() throws JsonProcessingException, InvalidCredentialFormatException {
        String procedureId = "550e8400-e29b-41d4-a716-446655440000";
        String credentialString = "validCredentialString";

        LEARCredentialEmployee learCredentialEmployee = mock(LEARCredentialEmployee.class);

        when(objectMapper.readValue(credentialString, LEARCredentialEmployee.class)).thenReturn(learCredentialEmployee);
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn("cloud");

        when(remoteSignatureServiceImpl.validateCredentials())
                .thenReturn(Mono.error(new IllegalArgumentException("Non-recoverable error")));

        when(remoteSignatureServiceImpl.isRecoverableError(any())).thenReturn(false);

        when(remoteSignatureServiceImpl.handlePostRecoverError(any(), eq(procedureId)))
                .thenReturn(Mono.empty());

        StepVerifier.create(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(credentialString, procedureId))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(RemoteSignatureException.class, throwable);
                    Assertions.assertEquals("Signature Failed, changed to ASYNC mode", throwable.getMessage());
                })
                .verify();

        verify(remoteSignatureServiceImpl, times(1)).validateCredentials();
        verify(remoteSignatureServiceImpl, times(1)).handlePostRecoverError(any(), eq(procedureId));
    }
    @Disabled
    @Test
    void mapCredentialAndBindIssuerInToTheCredential_HandlePostRecoverErrorFails() throws JsonProcessingException, InvalidCredentialFormatException {
        String procedureId = "550e8400-e29b-41d4-a716-446655440000";
        String credentialString = "validCredentialString";

        LEARCredentialEmployee learCredentialEmployee = mock(LEARCredentialEmployee.class);

        when(objectMapper.readValue(credentialString, LEARCredentialEmployee.class)).thenReturn(learCredentialEmployee);
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn("cloud");

        when(remoteSignatureServiceImpl.validateCredentials())
                .thenAnswer(invocation -> Mono.error(new ConnectException("Connection timeout")));

        when(remoteSignatureServiceImpl.isRecoverableError(any())).thenReturn(true);
        when(remoteSignatureServiceImpl.handlePostRecoverError(any(), eq(procedureId)))
                .thenReturn(Mono.error(new RuntimeException("Error in post-recovery handling")));

        StepVerifier.create(learCredentialEmployeeFactory.mapCredentialAndBindIssuerInToTheCredential(credentialString, procedureId))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(RuntimeException.class, throwable);
                    Assertions.assertEquals("Error in post-recovery handling", throwable.getMessage());
                })
                .verify();

        verify(remoteSignatureServiceImpl, times(4)).validateCredentials();
        verify(remoteSignatureServiceImpl).handlePostRecoverError(any(), eq(procedureId));
    }


}