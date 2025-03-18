package es.in2.issuer.infrastructure.controller;

import es.in2.issuer.application.workflow.VerifiableCredentialIssuanceWorkflow;
import es.in2.issuer.domain.model.dto.IssuanceRequest;
import es.in2.issuer.domain.service.AccessTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


class IssuanceControllerTest {

    private VerifiableCredentialIssuanceWorkflow mockWorkflow;
    private AccessTokenService mockAccessTokenService;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        mockWorkflow = mock(VerifiableCredentialIssuanceWorkflow.class);
        mockAccessTokenService = mock(AccessTokenService.class);

        IssuanceController issuanceController = new IssuanceController(mockWorkflow, mockAccessTokenService);
        webTestClient = WebTestClient.bindToController(issuanceController).build();
    }

    @Test
    void testInternalIssueCredential_Success() {
        String mockToken = "mockBearerToken";
        IssuanceRequest request = new IssuanceRequest("schema123", "jwt_vc_json", null, "auto", 365, "https://callback.com");

        when(mockAccessTokenService.getCleanBearerToken(anyString())).thenReturn(Mono.just(mockToken));
        when(mockWorkflow.completeIssuanceCredentialProcess(anyString(), anyString(), any(IssuanceRequest.class), anyString()))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/vci/v1/issuances")
                .header(HttpHeaders.AUTHORIZATION, "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        verify(mockAccessTokenService, times(1)).getCleanBearerToken("Bearer mock-token");

        ArgumentCaptor<String> processIdCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockWorkflow, times(1)).completeIssuanceCredentialProcess(
                processIdCaptor.capture(),
                eq("schema123"),
                eq(request),
                eq(mockToken)
        );

        assert !processIdCaptor.getValue().isEmpty();
    }

    @Test
    void testExternalIssueCredential_Success() {
        String mockToken = "mockBearerToken";
        IssuanceRequest request = new IssuanceRequest("schema123", "jwt_vc_json", null, "auto", 365, "https://callback.com");

        when(mockAccessTokenService.getCleanBearerToken(anyString())).thenReturn(Mono.just(mockToken));
        when(mockWorkflow.completeIssuanceCredentialProcess(anyString(), anyString(), any(IssuanceRequest.class), anyString()))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/vci/v1/issuances/external")
                .header(HttpHeaders.AUTHORIZATION, "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        verify(mockAccessTokenService, times(1)).getCleanBearerToken("Bearer mock-token");
    }
}