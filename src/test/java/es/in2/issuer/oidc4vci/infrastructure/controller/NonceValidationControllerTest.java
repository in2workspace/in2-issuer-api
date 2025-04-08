package es.in2.issuer.oidc4vci.infrastructure.controller;

import es.in2.issuer.oidc4vci.domain.service.NonceValidationService;
import es.in2.issuer.shared.domain.model.dto.NonceValidationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;

@WithMockUser
@MockBean(ReactiveAuthenticationManager.class)
@WebFluxTest(NonceValidationController.class)
class NonceValidationControllerTest {

    @MockBean
    NonceValidationService nonceValidationService;

    @Autowired
    WebTestClient webTestClient;

    @Test
    void testValidateNonceSuccess() {
        String nonce = "1234";

        NonceValidationResponse nonceValidationResponse = new NonceValidationResponse(true);

        when(nonceValidationService.validate(nonce))
                .thenReturn(Mono.just(nonceValidationResponse));

        webTestClient
                .mutateWith(csrf())
                .post()
                .uri("/nonce-valid")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("nonce", nonce))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(NonceValidationResponse.class)
                .isEqualTo(nonceValidationResponse);
    }
}