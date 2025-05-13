package es.in2.issuer.backend.backoffice.infrastructure.controller;

import es.in2.issuer.backend.backoffice.domain.model.dtos.SignatureConfigAudit;
import es.in2.issuer.backend.backoffice.domain.service.SignatureConfigurationAuditService;
import es.in2.issuer.backend.shared.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ops/v1/signatures/configs/audit")
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
    public Flux<SignatureConfigAudit> getAllAudits(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
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
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam String organizationIdentifier
    ) {
        return auditService.getAuditsByOrganization(organizationIdentifier);
    }

}