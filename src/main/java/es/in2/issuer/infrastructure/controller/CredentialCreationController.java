package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.service.VerifiableCredentialIssuanceService;
import es.in2.issuer.domain.model.LEARCredentialRequest;
import es.in2.issuer.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/credentials")
@RequiredArgsConstructor
public class CredentialCreationController {
    private final VerifiableCredentialIssuanceService verifiableCredentialIssuanceService;

    @Operation(
            summary = "Creates a withdraw credential",
            description = "Generates a a withdraw credential and sends a notification to the appointed employee",
            tags = {SwaggerConfig.TAG_PUBLIC}
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Returns Created when the creation was successfully"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "The request is invalid or missing params Ensure the 'Authorization' header is set with a valid Bearer Token."
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "This response is returned when an unexpected server error occurs."
                    )
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> createWithdrawLEARCredential(@RequestParam String type, @RequestBody LEARCredentialRequest learCredentialRequest) {
        String processId = UUID.randomUUID().toString();
        return verifiableCredentialIssuanceService.completeWithdrawLearCredentialProcess(processId, type, learCredentialRequest);
    }
}
