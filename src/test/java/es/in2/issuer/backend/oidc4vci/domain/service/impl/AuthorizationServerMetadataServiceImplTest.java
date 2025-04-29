package es.in2.issuer.backend.oidc4vci.domain.service.impl;

import es.in2.issuer.backend.shared.infrastructure.config.AppConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorizationServerMetadataServiceImplTest {

    private static final String PROCESS_ID = "b731b463-7473-4f97-be7a-658ec0b5dbc9";
    private static final String ISSUER_URL = "https://issuer.example.com";

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private AuthorizationServerMetadataServiceImpl authorizationServerMetadataService;

    @Test
    void shouldBuildAuthorizationServerMetadataCorrectly() {
        // Arrange
        when(appConfig.getIssuerBackendUrl()).thenReturn(ISSUER_URL);
        // Act
        var resultMono = authorizationServerMetadataService.buildAuthorizationServerMetadata(PROCESS_ID);
        // Assert
        StepVerifier.create(resultMono)
                .assertNext(metadata -> {
                    assertThat(metadata.issuer()).isEqualTo(ISSUER_URL);
                    assertThat(metadata.tokenEndpoint()).isEqualTo(ISSUER_URL + "/oauth/token");
                    assertThat(metadata.responseTypesSupported()).containsExactly("token");
                    assertThat(metadata.preAuthorizedGrantAnonymousAccessSupported()).isTrue();
                })
                .verifyComplete();
    }

}
