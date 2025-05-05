package es.in2.issuer.backend.oidc4vci.application.workflow.impl;

import es.in2.issuer.backend.oidc4vci.domain.model.AuthorizationServerMetadata;
import es.in2.issuer.backend.oidc4vci.domain.service.AuthorizationServerMetadataService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAuthorizationServerMetadataWorkflowImplTest {

    @Mock
    private AuthorizationServerMetadataService authorizationServerMetadataService;

    @InjectMocks
    private GetAuthorizationServerMetadataWorkflowImpl getAuthorizationServerMetadataWorkflowImpl;

    @Test
    void testExecute() {
        // Arrange
        String processId = "b731b463-7473-4f97-be7a-658ec0b5dbc9";
        AuthorizationServerMetadata expectedAuthorizationServerMetadata = AuthorizationServerMetadata.builder()
                .issuer("https://issuer.example.com")
                .tokenEndpoint("https://issuer.example.com/oauth/token")
                .responseTypesSupported(Set.of("token"))
                .preAuthorizedGrantAnonymousAccessSupported(true)
                .build();
        // Mock
        when(getAuthorizationServerMetadataWorkflowImpl.execute(processId))
                .thenReturn(Mono.just(expectedAuthorizationServerMetadata));
        // Act
        Mono<AuthorizationServerMetadata> result = getAuthorizationServerMetadataWorkflowImpl.execute(processId);
        // Assert
        assertEquals(expectedAuthorizationServerMetadata, result.block());
    }

}