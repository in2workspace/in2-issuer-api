package es.in2.issuer.backoffice.infrastructure.controller;

import es.in2.issuer.backoffice.domain.model.dto.CredentialDetails;
import es.in2.issuer.backoffice.domain.model.dto.CredentialProcedures;
import es.in2.issuer.backoffice.domain.model.dto.ProcedureBasicInfo;
import es.in2.issuer.backoffice.domain.service.AccessTokenService;
import es.in2.issuer.backoffice.domain.service.CredentialProcedureService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialManagementControllerTest {

    @Mock
    private CredentialProcedureService credentialProcedureService;

    @Mock
    private AccessTokenService accessTokenService;

    @InjectMocks
    private CredentialManagementController credentialManagementController;

    @Test
    void getAllProcedures() {
        //Arrange
        String organizationId = "testOrganizationId";
        ProcedureBasicInfo procedureBasicInfo = ProcedureBasicInfo.builder()
                .procedureId(UUID.randomUUID())
                .subject("testFullName")
                .status("testStatus")
                .updated(new Timestamp(System.currentTimeMillis()))
                .build();
        CredentialProcedures.CredentialProcedure credentialProcedure = new CredentialProcedures.CredentialProcedure(procedureBasicInfo);
        CredentialProcedures credentialProcedures = CredentialProcedures.builder().credentialProcedures(List.of(credentialProcedure)).build();
        when(accessTokenService.getOrganizationId(anyString())).thenReturn(Mono.just(organizationId));
        when(credentialProcedureService.getAllProceduresBasicInfoByOrganizationId(organizationId)).thenReturn(Mono.just(credentialProcedures));

        //Act
        Mono<CredentialProcedures> result = credentialManagementController.getAllProcedures("Bearer testToken");

        //Assert
        StepVerifier.create(result)
                .assertNext(procedures -> assertEquals(credentialProcedures, procedures))
                .verifyComplete();
    }

    @Test
    void getProcedure() {
        //Arrange
        String organizationId = "testOrganizationId";
        String procedureId = "testProcedureId";
        CredentialDetails credentialDetails = CredentialDetails.builder()
                .procedureId(UUID.randomUUID())
                .credentialStatus("testCredentialStatus")
                .credential(null)
                .build();
        when(accessTokenService.getOrganizationId(anyString())).thenReturn(Mono.just(organizationId));
        when(credentialProcedureService.getProcedureDetailByProcedureIdAndOrganizationId(organizationId, procedureId)).thenReturn(Mono.just(credentialDetails));

        //Act
        Mono<CredentialDetails> result = credentialManagementController.getProcedure("Bearer testToken", procedureId);

        //Assert
        StepVerifier.create(result)
                .assertNext(details -> assertEquals(credentialDetails, details))
                .verifyComplete();
    }
}