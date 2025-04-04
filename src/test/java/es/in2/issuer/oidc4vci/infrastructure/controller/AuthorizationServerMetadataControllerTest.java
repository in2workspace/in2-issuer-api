package es.in2.issuer.oidc4vci.infrastructure.controller;

import es.in2.issuer.oidc4vci.domain.model.dto.AuthorizationServerMetadata;
import es.in2.issuer.oidc4vci.domain.service.AuthorizationServerMetadataService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@WithMockUser
@MockBean(ReactiveAuthenticationManager.class)
@WebFluxTest(AuthorizationServerMetadataController.class)
class AuthorizationServerMetadataControllerTest {

    @MockBean
    private AuthorizationServerMetadataService authorizationServerMetadataService;

    @Autowired
    WebTestClient webTestClient;

    @Test
    void testGetEntitiesSuccess() {
        AuthorizationServerMetadata expectedAuthorizationServerMetadata =
                new AuthorizationServerMetadata("https://example.org/token");
        when(authorizationServerMetadataService.generateOpenIdAuthorizationServerMetadata())
                .thenReturn(Mono.just(expectedAuthorizationServerMetadata));

        webTestClient
                .get()
                .uri("/.well-known/openid-configuration")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AuthorizationServerMetadata.class)
                .isEqualTo(expectedAuthorizationServerMetadata);
    }
}