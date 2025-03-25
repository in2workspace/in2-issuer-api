package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.CredentialSignerWorkflow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignUnsignedCredentialControllerTest {

    @Mock
    private CredentialSignerWorkflow credentialSignerWorkflow;

    private WebTestClient webTestClient;

    @BeforeEach
    void setup() {
        SignUnsignedCredentialController controller = new SignUnsignedCredentialController(credentialSignerWorkflow);
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void testSignUnsignedCredential_Success() {
        String authorizationHeader = "Bearer token";
        String procedureId = "d290f1ee-6c54-4b01-90e6-d701748f0851";

        when(credentialSignerWorkflow.retrySignUnsignedCredential(authorizationHeader, procedureId))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/v1/retry-sign-credential/" + procedureId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .exchange()
                .expectStatus().isCreated();

        verify(credentialSignerWorkflow).retrySignUnsignedCredential(authorizationHeader, procedureId);
    }

    @Test
    void testSignUnsignedCredential_ErrorFromWorkflow() {
        String authorizationHeader = "Bearer token";
        String procedureId = "d290f1ee-6c54-4b01-90e6-d701748f0851";

        when(credentialSignerWorkflow.retrySignUnsignedCredential(authorizationHeader, procedureId))
                .thenReturn(Mono.error(new RuntimeException("Simulated error")));

        webTestClient.post()
                .uri("/api/v1/retry-sign-credential/" + procedureId)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}