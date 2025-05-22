package es.in2.issuer.backend.shared.domain.util.factory;

import es.in2.issuer.backend.shared.domain.model.dto.credential.DetailedIssuer;
import es.in2.issuer.backend.shared.domain.service.impl.RemoteSignatureServiceImpl;
import es.in2.issuer.backend.shared.infrastructure.config.DefaultSignerConfig;
import es.in2.issuer.backend.shared.infrastructure.config.RemoteSignatureConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static es.in2.issuer.backend.backoffice.domain.util.Constants.*;
import static es.in2.issuer.backend.shared.domain.util.Constants.LEAR_CREDENTIAL_EMPLOYEE;
import static es.in2.issuer.backend.shared.domain.util.Constants.VERIFIABLE_CERTIFICATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssuerFactoryTest {

    @Mock private RemoteSignatureConfig remoteSignatureConfig;
    @Mock private DefaultSignerConfig defaultSignerConfig;
    @Mock private RemoteSignatureServiceImpl remoteSignatureServiceImpl;
    @InjectMocks private IssuerFactory issuerFactory;

    private final String procedureId = "proc-123";
    private final String learType = LEAR_CREDENTIAL_EMPLOYEE;
    private final String verifiableType = VERIFIABLE_CERTIFICATION;

    @BeforeEach
    void setUp() {
        // Per omissió, cap valor: no anem per la via SERVER
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn("OTHER");
    }

    @Test
    void createIssuer_LocalServerSide_ReturnsFromDefaultConfig() {
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn(SIGNATURE_REMOTE_TYPE_SERVER);
        when(defaultSignerConfig.getOrganizationIdentifier()).thenReturn("ORG-ID");
        when(defaultSignerConfig.getOrganization()).thenReturn("MyOrg");
        when(defaultSignerConfig.getCountry()).thenReturn("ES");
        when(defaultSignerConfig.getCommonName()).thenReturn("CN");
        when(defaultSignerConfig.getEmail()).thenReturn("a@b.c");
        when(defaultSignerConfig.getSerialNumber()).thenReturn("SN123");

        StepVerifier.create(issuerFactory.createIssuer(procedureId, learType))
                .assertNext(issuer -> {
                    assertEquals(DID_ELSI + "ORG-ID", issuer.id());
                    assertEquals("ORG-ID", issuer.organizationIdentifier());
                    assertEquals("MyOrg", issuer.organization());
                    assertEquals("ES", issuer.country());
                    assertEquals("CN", issuer.commonName());
                    assertEquals("a@b.c", issuer.emailAddress());
                    assertEquals("SN123", issuer.serialNumber());
                })
                .verifyComplete();
    }

    @Test
    void createIssuer_Remote_CredentialsMismatch_CompletesSilently() {
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn(SIGNATURE_REMOTE_TYPE_CLOUD);
        when(remoteSignatureServiceImpl.validateCredentials()).thenReturn(Mono.just(false));
        when(remoteSignatureServiceImpl.handlePostRecoverError(procedureId)).thenReturn(Mono.empty());

        StepVerifier.create(
                        issuerFactory.createIssuer(procedureId, learType)
                )
                .expectComplete()
                .verify();

        verify(remoteSignatureServiceImpl).validateCredentials();
        verify(remoteSignatureServiceImpl).handlePostRecoverError(procedureId);
    }

    @Test
    void createIssuerRemote_LearCredentialEmployee_SuccessPath() {
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn(SIGNATURE_REMOTE_TYPE_CLOUD);
        when(remoteSignatureServiceImpl.validateCredentials()).thenReturn(Mono.just(true));
        when(remoteSignatureServiceImpl.getMandatorMail(procedureId)).thenReturn(Mono.just("mandator@mail"));
        when(remoteSignatureServiceImpl.requestAccessToken(any(), eq(SIGNATURE_REMOTE_SCOPE_SERVICE)))
                .thenReturn(Mono.just("token"));
        when(remoteSignatureServiceImpl.requestCertificateInfo("token", "cred-id"))
                .thenReturn(Mono.just("cert-info"));
        DetailedIssuer expected = DetailedIssuer.builder()
                .id("id1").organizationIdentifier("org1").organization("o")
                .country("c").commonName("cn").emailAddress("e").serialNumber("sn")
                .build();
        when(remoteSignatureConfig.getRemoteSignatureCredentialId()).thenReturn("cred-id");
        when(remoteSignatureServiceImpl.extractIssuerFromCertificateInfo("cert-info", "mandator@mail"))
                .thenReturn(Mono.just(expected));

        StepVerifier.create(issuerFactory.createIssuer(procedureId, learType))
                .expectNext(expected)
                .verifyComplete();
    }

    @Test
    void createIssuerRemote_VerifiableCertification_SuccessPath() {
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn(SIGNATURE_REMOTE_TYPE_CLOUD);
        when(remoteSignatureServiceImpl.validateCredentials()).thenReturn(Mono.just(true));
        when(remoteSignatureServiceImpl.getMailForVerifiableCertification(procedureId)).thenReturn(Mono.just("vc@mail"));
        when(remoteSignatureServiceImpl.requestAccessToken(any(), eq(SIGNATURE_REMOTE_SCOPE_SERVICE)))
                .thenReturn(Mono.just("token2"));
        when(remoteSignatureServiceImpl.requestCertificateInfo("token2", "vc-cred"))
                .thenReturn(Mono.just("vc-cert"));
        DetailedIssuer expectedVC = DetailedIssuer.builder()
                .id("id2").organizationIdentifier("org2").organization("o2")
                .country("c2").commonName("cn2").emailAddress("e2").serialNumber("sn2")
                .build();
        when(remoteSignatureConfig.getRemoteSignatureCredentialId()).thenReturn("vc-cred");
        when(remoteSignatureServiceImpl.extractIssuerFromCertificateInfo("vc-cert", "vc@mail"))
                .thenReturn(Mono.just(expectedVC));

        StepVerifier.create(issuerFactory.createIssuer(procedureId, verifiableType))
                .expectNext(expectedVC)
                .verifyComplete();
    }

    @Test
    void createIssuerRemote_UnsupportedCredentialType_EmitsError() {
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn(SIGNATURE_REMOTE_TYPE_CLOUD);
        when(remoteSignatureServiceImpl.validateCredentials()).thenReturn(Mono.just(true));
        when(remoteSignatureServiceImpl.handlePostRecoverError(procedureId)).thenReturn(Mono.empty());

        StepVerifier.create(
                        issuerFactory.createIssuer(procedureId, "UNKNOWN_TYPE")
                )
                .expectComplete()
                .verify();
    }

    @Test
    void createIssuerRemote_ValidateCredentialsRecoverable_ThenHandlePostRecoverCompletes() {
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn(SIGNATURE_REMOTE_TYPE_CLOUD);
        when(remoteSignatureServiceImpl.validateCredentials())
                .thenReturn(Mono.error(new IOException("timeout")))
                .thenReturn(Mono.error(new IOException("timeout2")))
                .thenReturn(Mono.error(new IOException("timeout3")))
                .thenReturn(Mono.error(new IOException("timeout4")));
        when(remoteSignatureServiceImpl.isRecoverableError(any())).thenReturn(true);
        when(remoteSignatureServiceImpl.handlePostRecoverError(procedureId)).thenReturn(Mono.empty());

        StepVerifier.create(issuerFactory.createIssuer(procedureId, learType))
                .verifyComplete();

        verify(remoteSignatureServiceImpl, times(4)).validateCredentials();
        verify(remoteSignatureServiceImpl).handlePostRecoverError(procedureId);
    }

    @Test
    void createIssuerRemote_ValidateCredentialsRecoverable_ThenHandlePostRecoverFails() {
        when(remoteSignatureConfig.getRemoteSignatureType()).thenReturn(SIGNATURE_REMOTE_TYPE_CLOUD);
        when(remoteSignatureServiceImpl.validateCredentials())
                .thenReturn(Mono.error(new IOException("timeout1")));
        when(remoteSignatureServiceImpl.isRecoverableError(any())).thenReturn(true);
        RuntimeException postEx = new RuntimeException("post-recover failed");
        when(remoteSignatureServiceImpl.handlePostRecoverError(procedureId))
                .thenReturn(Mono.error(postEx));

        StepVerifier.create(issuerFactory.createIssuer(procedureId, learType))
                .expectErrorMatches(ex -> ex == postEx)
                .verify();

        verify(remoteSignatureServiceImpl).handlePostRecoverError(procedureId);
    }
}
