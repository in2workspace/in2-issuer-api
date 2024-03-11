package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.exception.VcTemplateDoesNotExistException;
import es.in2.issuer.domain.service.IssuerVcTemplateService;
import es.in2.issuer.infrastructure.controller.VcTemplateController;
import id.walt.credentials.w3c.templates.VcTemplate;
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
class VcTemplateControllerTest {

    @Mock
    private IssuerVcTemplateService issuerVcTemplateService;

    @InjectMocks
    private VcTemplateController controller;

    @Test
    void testGetAllVcTemplatesByName_Success() {
        // Arrange
        List<VcTemplate> mockTemplateList = List.of(
                new VcTemplate("LegalPerson", null, false),
                new VcTemplate("Email", null, false)
        );
        when(issuerVcTemplateService.getAllVcTemplates()).thenReturn(Mono.just(mockTemplateList));

        // Act
        Mono<List<VcTemplate>> result = controller.getAllVcTemplatesByName();

        // Assert
        result.subscribe(templates -> assertEquals(mockTemplateList, templates));
        verify(issuerVcTemplateService, times(1)).getAllVcTemplates();
    }

    @Test
    void testGetAllVcTemplatesDetail_Success() {
        // Arrange
        List<VcTemplate> mockDetailedTemplateList = List.of(
                new VcTemplate("LegalPerson", null, false),
                new VcTemplate("Email", null, false)
        );
        when(issuerVcTemplateService.getAllDetailedVcTemplates()).thenReturn(Mono.just(mockDetailedTemplateList));

        // Act
        Mono<List<VcTemplate>> result = controller.getAllVcTemplatesDetail();

        // Assert
        result.subscribe(detailedTemplates -> assertEquals(mockDetailedTemplateList, detailedTemplates));
        verify(issuerVcTemplateService, times(1)).getAllDetailedVcTemplates();
    }

    @Test
    void testGetTemplateByName_Success() {
        // Arrange
        String templateName = "LegalPerson";
        VcTemplate mockTemplate = new VcTemplate(templateName, null, false);
        when(issuerVcTemplateService.getTemplate(templateName)).thenReturn(Mono.just(mockTemplate));

        // Act
        Mono<VcTemplate> result = controller.getTemplateByName(templateName);

        // Assert
        result.subscribe(template -> assertEquals(mockTemplate, template));
        verify(issuerVcTemplateService, times(1)).getTemplate(templateName);
    }

    @Test
    void testGetTemplateByName_NotFound() {
        // Arrange
        String nonExistentTemplateName = "NonExistentTemplate";
        when(issuerVcTemplateService.getTemplate(nonExistentTemplateName)).thenReturn(Mono.error(new VcTemplateDoesNotExistException("Template: '" + nonExistentTemplateName + "' is not supported")));

        // Act
        Mono<VcTemplate> result = controller.getTemplateByName(nonExistentTemplateName);

        // Assert
        result.subscribe(
                template -> fail("Expected an error to be thrown"),
                error -> assertTrue(error instanceof VcTemplateDoesNotExistException)
        );
        verify(issuerVcTemplateService, times(1)).getTemplate(nonExistentTemplateName);
    }

    @Test
    void testGetTemplateByName_ExceptionThrown() {
        // Arrange
        String templateName = "TemplateName";
        when(issuerVcTemplateService.getTemplate(templateName)).thenThrow(new RuntimeException("Some unexpected exception"));

        // Act
        Mono<VcTemplate> result = controller.getTemplateByName(templateName);

        // Assert
        result.subscribe(
                template -> fail("Expected an error to be thrown"),
                error -> {
                    assertTrue(error instanceof VcTemplateDoesNotExistException);
                    assertEquals("Template: '" + templateName + "' is not supported", error.getMessage());
                }
        );
        verify(issuerVcTemplateService, times(1)).getTemplate(templateName);
    }
}
