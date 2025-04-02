package es.in2.issuer.backend.domain.service;

import es.in2.issuer.backend.domain.model.dto.CredentialConfiguration;
import es.in2.issuer.backend.domain.model.dto.CredentialDefinition;
import es.in2.issuer.backend.domain.model.dto.CredentialIssuerMetadata;
import es.in2.issuer.backend.domain.service.impl.CredentialIssuerMetadataServiceImpl;
import es.in2.issuer.backend.domain.util.Constants;
import es.in2.issuer.backend.domain.util.EndpointsConstants;
import es.in2.issuer.backend.infrastructure.config.AppConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CredentialIssuerMetadataServiceImplTest {

    private final String issuerApiExternalDomain = "http://example.com";

    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private CredentialIssuerMetadataServiceImpl credentialIssuerMetadataService;

    @BeforeEach
    void setup() {
        lenient().when(appConfig.getIssuerApiExternalDomain()).thenReturn(issuerApiExternalDomain);
    }

    @Test
    void testGenerateOpenIdCredentialIssuer() {
        Mono<CredentialIssuerMetadata> credentialIssuerMetadataMono = credentialIssuerMetadataService.generateOpenIdCredentialIssuer();

        StepVerifier.create(credentialIssuerMetadataMono)
                .assertNext(metadata -> {
                    assertEquals(issuerApiExternalDomain, metadata.credentialIssuer(), "Credential Issuer");
                    assertEquals(issuerApiExternalDomain + EndpointsConstants.CREDENTIAL, metadata.credentialEndpoint(), "Credential Endpoint");
                    assertEquals(issuerApiExternalDomain + EndpointsConstants.CREDENTIAL_BATCH, metadata.batchCredentialEndpoint(), "Batch Credential Endpoint");
                    assertEquals(issuerApiExternalDomain + EndpointsConstants.CREDENTIAL_DEFERRED, metadata.deferredCredentialEndpoint(), "Deferred Credential Endpoint");

                    CredentialConfiguration config = metadata.credentialConfigurationsSupported().get(Constants.LEAR_CREDENTIAL_EMPLOYEE);
                    assertNotNull(config);
                    assertEquals(Constants.JWT_VC_JSON, config.format(), "Format");
                    assertTrue(config.cryptographicBindingMethodsSupported().isEmpty(), "Cryptographic Binding Methods Supported");
                    assertTrue(config.credentialSigningAlgValuesSupported().isEmpty(), "Credential Signing Alg Values Supported");

                    CredentialDefinition definition = config.credentialDefinition();
                    assertNotNull(definition);
                    assertEquals(List.of(Constants.LEAR_CREDENTIAL, Constants.VERIFIABLE_CREDENTIAL), definition.type(), "Credential Definition Types");
                })
                .verifyComplete();
    }
}