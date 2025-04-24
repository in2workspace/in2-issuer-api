package es.in2.issuer.backoffice.infrastructure.controller;

import es.in2.issuer.backoffice.application.workflow.CredentialOfferIssuanceWorkflow;
import es.in2.issuer.shared.domain.model.dto.CredentialErrorResponse;
import es.in2.issuer.backoffice.domain.model.dto.CredentialOfferUriResponse;
import es.in2.issuer.shared.domain.model.dto.CustomCredentialOffer;
import es.in2.issuer.shared.domain.model.dto.GlobalErrorMessage;
import es.in2.issuer.shared.infrastructure.config.SwaggerConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static es.in2.issuer.shared.domain.util.Constants.ENGLISH;

@Slf4j
@RestController
@RequestMapping("/oid4vci/v1/credential-offer")
@RequiredArgsConstructor
public class CredentialOfferController {

    private final CredentialOfferIssuanceWorkflow credentialOfferIssuanceWorkflow;

    // todo: backoffice --> /backoffice/v1/credential-offer/transaction-code/{id}
    @GetMapping("/transaction-code/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<CredentialOfferUriResponse> getCredentialOfferByTransactionCode(@PathVariable("id") String transactionCode) {
        log.info("Retrieving Credential Offer with Transaction Code...");
        String processId = UUID.randomUUID().toString();
        return credentialOfferIssuanceWorkflow.buildCredentialOfferUri(processId, transactionCode)
                .doOnSuccess(credentialOfferUri -> {
                            log.debug("Credential Offer URI created successfully: {}", credentialOfferUri);
                            log.info("Credential Offer created successfully.");
                        }
                );
    }

    // todo: backoffice --> /backoffice/v1/credential-offer/c-transaction-code/{id}
    @GetMapping("/c-transaction-code/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<CredentialOfferUriResponse> getCredentialOfferByCTransactionCode(@PathVariable("id") String cTransactionCode) {
        log.info("Retrieving Credential Offer with C Transaction Code...");
        String processId = UUID.randomUUID().toString();
        return credentialOfferIssuanceWorkflow.buildNewCredentialOfferUri(processId, cTransactionCode);
    }

    // todo: oid4vci --> credential_offer_uri /oid4vci/v1/credential-offer/{id}
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<CustomCredentialOffer> getCredentialOffer(@PathVariable("id") String id, ServerWebExchange exchange) {
        log.info("Getting Credential Offer...");
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(HttpHeaders.CONTENT_LANGUAGE, ENGLISH);
        // todo: CredentialOfferWorkflow.retrieve(id)
        return credentialOfferIssuanceWorkflow.getCustomCredentialOffer(id);
    }

}
