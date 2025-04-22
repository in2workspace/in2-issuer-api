package es.in2.issuer.oidc4vci.domain.service.impl;

import es.in2.issuer.oidc4vci.domain.model.dto.AuthorizationServerMetadata;
import es.in2.issuer.shared.infrastructure.config.AppConfig;
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

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private AuthorizationServerMetadataServiceImpl authorizationServerMetadataService;

    @Test
    void itShouldGenerateOpenIdAuthorizationServerMetadata() {
        String expectedUrl = "https://example.com";
        AuthorizationServerMetadata expectedAuthorizationServerMetadata =
                new AuthorizationServerMetadata(expectedUrl + "/oauth/token");

        when(appConfig.getIssuerApiExternalDomain()).thenReturn(expectedUrl);

        var resultMono = authorizationServerMetadataService.generateOpenIdAuthorizationServerMetadata();

        StepVerifier.create(resultMono)
                .assertNext(result ->
                        assertThat(result).isEqualTo(expectedAuthorizationServerMetadata))
                .verifyComplete();
    }
}