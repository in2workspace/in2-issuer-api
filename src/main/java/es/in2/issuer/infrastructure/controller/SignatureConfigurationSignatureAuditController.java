package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.domain.model.dto.SignatureConfigAudit;
import es.in2.issuer.domain.model.entities.SignatureConfigurationAudit;
import es.in2.issuer.domain.service.SignatureConfigurationAuditService;
import es.in2.issuer.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/signatures/configs/audit")
@RequiredArgsConstructor
public class SignatureConfigurationSignatureAuditController {

    private final SignatureConfigurationAuditService auditService;

    @Operation(
            summary = "Get all signature configuration audits",
            description = "Returns all audit logs for signature configurations.",
            tags = {SwaggerConfig.TAG_PRIVATE}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audits retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
    })
    @GetMapping
    public Flux<SignatureConfigAudit> getAllAudits() {
        return auditService.getAllAudits();
    }

    @Operation(
            summary = "Get signature configuration audits by organization",
            description = "Returns audit logs filtered by organization identifier.",
            tags = {SwaggerConfig.TAG_PRIVATE}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Audits retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid organization identifier", content = @Content),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
    })
    @GetMapping(params = "organizationIdentifier")
    public Flux<SignatureConfigAudit> getAuditsByOrganization(
            @RequestParam String organizationIdentifier
    ) {
        return auditService.getAuditsByOrganization(organizationIdentifier);
    }

}
