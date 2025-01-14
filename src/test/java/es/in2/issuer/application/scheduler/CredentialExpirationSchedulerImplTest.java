package es.in2.issuer.application.scheduler;

import es.in2.issuer.domain.model.entities.CredentialProcedure;
import es.in2.issuer.domain.model.enums.CredentialStatus;
import es.in2.issuer.infrastructure.repository.CredentialProcedureRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Timestamp;
import java.time.Instant;

import static org.mockito.Mockito.*;

class CredentialExpirationSchedulerImplTest {

    @Mock
    private CredentialProcedureRepository credentialProcedureRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CredentialExpirationSchedulerImpl scheduler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldExpireCredentialsWhenValidUntilHasPassed() {
        System.out.println("Iniciando test: shouldExpireCredentialsWhenValidUntilHasPassed()");

        CredentialProcedure credential = new CredentialProcedure();
        credential.setCredentialId(java.util.UUID.randomUUID());
        credential.setCredentialStatus(CredentialStatus.VALID);
        credential.setValidUntil(Timestamp.from(Instant.now().minusSeconds(60)));

        System.out.println("Credencial inicial: ID=" + credential.getCredentialId() +
                ", Estado=" + credential.getCredentialStatus() + ", Expira en: " + credential.getValidUntil());

        when(credentialProcedureRepository.findAll()).thenReturn(Flux.just(credential));

        when(credentialProcedureRepository.save(any(CredentialProcedure.class)))
                .thenAnswer(invocation -> {
                    CredentialProcedure updatedCredential = invocation.getArgument(0);
                    System.out.println("Credencial expirada guardada: ID=" + updatedCredential.getCredentialId() +
                            ", Nuevo Estado=" + updatedCredential.getCredentialStatus());
                    return Mono.just(updatedCredential);
                });

        scheduler.checkAndExpireCredentials();

        verify(credentialProcedureRepository, atLeastOnce()).save(argThat(updatedCredential ->
                updatedCredential.getCredentialStatus() == CredentialStatus.EXPIRED));

        System.out.println("Test completado correctamente.");
    }

    @Test
    void shouldNotExpireCredentialsIfValidUntilHasNotPassed() {
        System.out.println("Iniciando test: shouldNotExpireCredentialsIfValidUntilHasNotPassed()");

        CredentialProcedure credential = new CredentialProcedure();
        credential.setCredentialId(java.util.UUID.randomUUID());
        credential.setCredentialStatus(CredentialStatus.VALID);
        credential.setValidUntil(Timestamp.from(Instant.now().plusSeconds(60)));

        System.out.println("Credencial inicial: ID=" + credential.getCredentialId() +
                ", Estado=" + credential.getCredentialStatus() + ", Expira en: " + credential.getValidUntil());

        when(credentialProcedureRepository.findAll()).thenReturn(Flux.just(credential));

        when(credentialProcedureRepository.save(any(CredentialProcedure.class)))
                .thenAnswer(invocation -> {
                    CredentialProcedure updatedCredential = invocation.getArgument(0);
                    System.out.println("No debería haberse guardado: ID=" + updatedCredential.getCredentialId() +
                            ", Estado=" + updatedCredential.getCredentialStatus());
                    return Mono.just(updatedCredential);
                });

        scheduler.checkAndExpireCredentials();

        verify(credentialProcedureRepository, never()).save(any(CredentialProcedure.class));
        assert credential.getCredentialStatus() == CredentialStatus.VALID :
                "ERROR: La credencial ha cambiado de estado incorrectamente.";

        System.out.println("Test completado correctamente, la credencial sigue siendo válida.");
    }
}
