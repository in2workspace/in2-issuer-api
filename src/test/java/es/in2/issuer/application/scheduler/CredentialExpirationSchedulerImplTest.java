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
import reactor.test.StepVerifier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    void shouldExpireCredentialsWhenValidUntilHasPassed() throws Exception {
        System.out.println("🔍 Iniciando test: shouldExpireCredentialsWhenValidUntilHasPassed()");

        String expiredCredentialJson = "{\"vc\": {\"validUntil\": \"" + Instant.now().minusSeconds(60) + "\"}}";

        CredentialProcedure credential = new CredentialProcedure();
        credential.setCredentialDecoded(expiredCredentialJson);
        credential.setCredentialStatus(CredentialStatus.VALID);

        System.out.println("📝 Credencial inicial: ID=" + credential.getCredentialId() + ", Estado=" + credential.getCredentialStatus());
        System.out.println("📝 JSON de la credencial: " + expiredCredentialJson);

        JsonNode jsonNode = new ObjectMapper().readTree(expiredCredentialJson);
        when(objectMapper.readTree(expiredCredentialJson)).thenReturn(jsonNode);

        when(credentialProcedureRepository.findAll()).thenReturn(Flux.just(credential));

        when(credentialProcedureRepository.save(any(CredentialProcedure.class)))
                .thenAnswer(invocation -> {
                    CredentialProcedure updatedCredential = invocation.getArgument(0);
                    System.out.println("✅ Credencial expirada guardada: ID=" + updatedCredential.getCredentialId() + ", Nuevo Estado=" + updatedCredential.getCredentialStatus());
                    return Mono.just(updatedCredential);
                });

        scheduler.checkAndExpireCredentials();

        verify(credentialProcedureRepository, atLeastOnce()).save(argThat(updatedCredential ->
                updatedCredential.getCredentialStatus() == CredentialStatus.EXPIRED));

        System.out.println("✅ Test completado correctamente.");
    }
}
