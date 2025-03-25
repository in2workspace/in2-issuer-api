package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.backend.domain.exception.VcTemplateDoesNotExistException;
import es.in2.issuer.backend.domain.model.dto.VcTemplate;
import es.in2.issuer.backend.domain.service.CredentialSchemaService;
import es.in2.issuer.backend.infrastructure.controller.CredentialSchemaController;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialSchemaControllerTest {

    @Mock
    private CredentialSchemaService credentialSchemaService;

    @InjectMocks
    private CredentialSchemaController controller;

    @Test
    void testGetAllVcTemplatesByName_Success() {
        // Arrange
        List<VcTemplate> mockTemplateList = List.of(
                new VcTemplate(false, "LegalPerson", null),
                new VcTemplate(false, "Email", null)
        );
        when(credentialSchemaService.getAllVcTemplates()).thenReturn(Mono.just(mockTemplateList));
        // Act
        Mono<List<VcTemplate>> result = controller.getAllCredentialSchemasByName();
        // Assert
        result.subscribe(templates -> assertEquals(mockTemplateList, templates));
        verify(credentialSchemaService, times(1)).getAllVcTemplates();
    }

    @Test
    void testGetAllVcTemplatesDetail_Success() {
        // Arrange
        List<VcTemplate> mockDetailedTemplateList = List.of(
                new VcTemplate(false, "LegalPerson", null),
                new VcTemplate(false, "Email", null)
        );
        when(credentialSchemaService.getAllDetailedVcTemplates()).thenReturn(Mono.just(mockDetailedTemplateList));
        // Act
        Mono<List<VcTemplate>> result = controller.getAllCredentialSchemaDetails();
        // Assert
        result.subscribe(detailedTemplates -> assertEquals(mockDetailedTemplateList, detailedTemplates));
        verify(credentialSchemaService, times(1)).getAllDetailedVcTemplates();
    }

    @Test
    void testGetTemplateByName_Success() {
        // Arrange
        String templateName = "LegalPerson";
        VcTemplate mockTemplate = new VcTemplate(false, templateName, null);
        when(credentialSchemaService.getTemplate(templateName)).thenReturn(Mono.just(mockTemplate));
        // Act
        Mono<VcTemplate> result = controller.getCredentialSchemaByName(templateName);
        // Assert
        result.subscribe(template -> assertEquals(mockTemplate, template));
        verify(credentialSchemaService, times(1)).getTemplate(templateName);
    }

    @Test
    void testGetTemplateByName_NotFound() {
        // Arrange
        String nonExistentTemplateName = "NonExistentTemplate";
        when(credentialSchemaService.getTemplate(nonExistentTemplateName))
                .thenReturn(Mono.error(new VcTemplateDoesNotExistException("Template: '" + nonExistentTemplateName + "' is not supported")));
        // Act
        Mono<VcTemplate> result = controller.getCredentialSchemaByName(nonExistentTemplateName);
        // Assert
        result.subscribe(
                template -> fail("Expected an error to be thrown"),
                error -> assertTrue(error instanceof VcTemplateDoesNotExistException)
        );
        verify(credentialSchemaService, times(1)).getTemplate(nonExistentTemplateName);
    }

}
