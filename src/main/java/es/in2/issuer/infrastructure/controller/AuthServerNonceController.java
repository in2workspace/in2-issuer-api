package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.service.VerifiableCredentialIssuanceService;
import es.in2.issuer.domain.model.AuthServerNonceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth-server-nonce")
@RequiredArgsConstructor
public class AuthServerNonceController {
    private final VerifiableCredentialIssuanceService verifiableCredentialIssuanceService;

    @GetMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> bindAccessTokenByPreAuthorizedCode (@RequestBody AuthServerNonceRequest authServerNonceRequest) {
        String processId = UUID.randomUUID().toString();
        return verifiableCredentialIssuanceService.bindAccessTokenByPreAuthorizedCode(processId,authServerNonceRequest);
    }
}
