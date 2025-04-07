package es.in2.issuer.oidc4vci.infrastructure.controller;

import es.in2.issuer.oidc4vci.domain.model.dto.TokenRequest;
import es.in2.issuer.oidc4vci.domain.model.dto.TokenResponse;
import es.in2.issuer.oidc4vci.domain.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @PostMapping(
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Mono<TokenResponse> getCredentialIssuerMetadata(TokenRequest tokenRequest) {
        return tokenService.generateTokenResponse(
                        tokenRequest.grantType(),
                        tokenRequest.preAuthorizedCode(),
                        tokenRequest.txCode());
    }
}