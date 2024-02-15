package es.in2.issuer.api.controller;

import es.in2.issuer.api.config.swagger.SwaggerConfig;
import es.in2.issuer.api.model.dto.CredentialResponseError;
import es.in2.issuer.api.exception.VcTemplateDoesNotExistException;
import es.in2.issuer.api.service.IssuerVcTemplateService;
import id.walt.credentials.w3c.templates.VcTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
@RestController
@RequestMapping("/api/vc-templates")
@RequiredArgsConstructor
public class VcTemplateController {

    private final IssuerVcTemplateService issuerVcTemplateService;

    @Operation(
            summary = "Retrieve All Verifiable Credential Templates",
            description = "Get a list of all available Verifiable Credential (VC) templates by name. These templates represent various types of verifiable credentials that can be issued by the credential issuer.",
            tags = {SwaggerConfig.TAG_PRIVATE}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful response with a list of VC templates",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = VcTemplate.class)), examples = @ExampleObject(name = "VC templates list", value = "[{\"name\":\"LegalPerson\",\"template\":null,\"mutable\":false},{\"name\":\"Email\",\"template\":null,\"mutable\":false}]"))
                    )
            }
    )
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Mono<List<VcTemplate>> getAllVcTemplatesByName() {
        return issuerVcTemplateService.getAllVcTemplates();
    }

    @Operation(
            summary = "Retrieve Detailed Information for All VC Templates",
            description = "Retrieve a list of all available Verifiable Credential (VC) templates with detailed information. This includes additional details and attributes associated with each VC template, such as schema information and template configuration.",
            tags = {"Private"}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful response with a detailed list of VC templates"
                    )
            }
    )
    @GetMapping("/detail")
    @ResponseStatus(HttpStatus.OK)
    public Mono<List<VcTemplate>> getAllVcTemplatesDetail() {
        return issuerVcTemplateService.getAllDetailedVcTemplates();
    }

    @Operation(
            summary = "Retrieve VC Template by Name",
            description = "Retrieve a specific Verifiable Credential (VC) template by its unique name. This endpoint allows you to retrieve detailed information about a specific VC template, including its schema and configuration, based on the provided template name.",
            tags = {"Private"}
    )
    @Parameter(
            name = "templateName",
            description = "The name of the template to retrieve",
            required = true,
            in = ParameterIn.PATH,
            schema = @Schema(type = "string")
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful response with a detailed list of VC templates"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "The VC Template does not exist",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = CredentialResponseError.class),
                                    examples = @ExampleObject(name = "unsupportedVCTemplate", value = "{\"error\": \"VC Template does not exist\", \"description\": \"Template: 'WrongTemplateName' is not supported\"}"))
                    )
            }
    )
    @GetMapping("/{templateName}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<VcTemplate> getTemplateByName(@PathVariable("templateName") String templateName) {
        try {
            return issuerVcTemplateService.getTemplate(templateName);
        } catch (Exception e) {
            return Mono.error(new VcTemplateDoesNotExistException("Template: '" + templateName + "' is not supported"));
        }
    }

}
