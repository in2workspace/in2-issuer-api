package es.in2.issuer.oidc4vci.infrastructure.controller;

import es.in2.issuer.oidc4vci.domain.model.dto.NonceValidationRequest;
import es.in2.issuer.oidc4vci.domain.service.NonceValidationService;
import es.in2.issuer.shared.domain.model.dto.NonceValidationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/nonce-valid")
@RequiredArgsConstructor
public class NonceValidationController {
    private final NonceValidationService nonceValidationService;

    @PostMapping(
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<NonceValidationResponse> getCredentialIssuerMetadata(NonceValidationRequest nonceRequest) {
        return nonceValidationService.validate(nonceRequest.nonce());
    }
}
