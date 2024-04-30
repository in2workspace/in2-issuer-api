package es.in2.issuer.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.entity.CredentialDeferred;
import es.in2.issuer.domain.entity.CredentialManagement;
import es.in2.issuer.domain.repository.CredentialDeferredRepository;
import es.in2.issuer.domain.repository.CredentialManagementRepository;
import es.in2.issuer.domain.service.impl.CredentialManagementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

import static es.in2.issuer.domain.util.Constants.CREDENTIAL_ISSUED;
import static es.in2.issuer.domain.util.Constants.JWT_VC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialManagementServiceImplTest {

    @Mock
    private CredentialManagementRepository credentialManagementRepository;

    @Mock
    private CredentialDeferredRepository credentialDeferredRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CredentialManagementServiceImpl credentialManagementService;

    private final String userId = "user-id";
    private final String credential = "{\"example\": \"data\"}";
    private final String format = "json";
    private CredentialManagement credentialManagement;
    private CredentialDeferred credentialDeferred;

    @BeforeEach
    void setUp() {
        credentialManagement = new CredentialManagement();
        credentialManagement.setId(UUID.randomUUID());
        credentialManagement.setUserId(userId);
        credentialManagement.setCredentialDecoded(credential);
        credentialManagement.setCredentialFormat(format);
        credentialManagement.setCredentialStatus("ISSUED");
        credentialManagement.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        credentialDeferred = new CredentialDeferred();
        credentialDeferred.setId(UUID.randomUUID());
        credentialDeferred.setCredentialId(credentialManagement.getId());
        credentialDeferred.setTransactionId("transaction-id");
    }

    @Test
    void testCommitCredential() {
        when(credentialManagementRepository.save(any(CredentialManagement.class))).thenReturn(Mono.just(credentialManagement));
        when(credentialDeferredRepository.save(any(CredentialDeferred.class))).thenReturn(Mono.just(credentialDeferred));

        StepVerifier.create(credentialManagementService.commitCredential(credential, userId, format))
                .expectNextMatches(transactionId -> !transactionId.isEmpty())
                .verifyComplete();

        verify(credentialManagementRepository).save(any(CredentialManagement.class));
        verify(credentialDeferredRepository).save(any(CredentialDeferred.class));
    }

    @Test
    void testUpdateCredential() {
        UUID credentialId = credentialManagement.getId();
        String credential = "some_encoded_credential_data";

        when(credentialManagementRepository.findByIdAndUserId(credentialId, userId)).thenReturn(Mono.just(credentialManagement));

        when(credentialManagementRepository.save(any(CredentialManagement.class))).thenReturn(Mono.just(credentialManagement));

        when(credentialDeferredRepository.findByCredentialId(credentialId)).thenReturn(Mono.just(new CredentialDeferred()));

        when(credentialDeferredRepository.save(any(CredentialDeferred.class))).thenReturn(Mono.just(new CredentialDeferred()));

        StepVerifier.create(credentialManagementService.updateCredential(credential, credentialId, userId))
                .verifyComplete();

        verify(credentialManagementRepository).findByIdAndUserId(credentialId, userId);
        verify(credentialManagementRepository).save(any(CredentialManagement.class));
        verify(credentialDeferredRepository).findByCredentialId(credentialId);
        verify(credentialDeferredRepository).save(any(CredentialDeferred.class));
    }

    @Test
    void testUpdateTransactionId() {
        when(credentialDeferredRepository.findByTransactionId("transaction-id")).thenReturn(Mono.just(credentialDeferred));
        when(credentialDeferredRepository.save(any(CredentialDeferred.class))).thenReturn(Mono.just(credentialDeferred));

        StepVerifier.create(credentialManagementService.updateTransactionId("transaction-id"))
                .expectNextMatches(newTransactionId -> !newTransactionId.isEmpty())
                .verifyComplete();

        verify(credentialDeferredRepository).findByTransactionId("transaction-id");
        verify(credentialDeferredRepository).save(any(CredentialDeferred.class));
    }

    @Test
    void testDeleteCredentialDeferred() {
        when(credentialDeferredRepository.findByTransactionId("transaction-id")).thenReturn(Mono.just(credentialDeferred));
        when(credentialDeferredRepository.delete(credentialDeferred)).thenReturn(Mono.empty());

        StepVerifier.create(credentialManagementService.deleteCredentialDeferred("transaction-id"))
                .verifyComplete();

        verify(credentialDeferredRepository).findByTransactionId("transaction-id");
        verify(credentialDeferredRepository).delete(credentialDeferred);
    }

    @Test
    void getCredentialsTest() throws JsonProcessingException {
        UUID credentialId = UUID.randomUUID();
        String userId = "user123";
        String jsonCredential = "{\"name\": \"John Doe\"}";
        Map<String, Object> parsedCredential = Map.of("name", "John Doe");

        CredentialManagement cm = new CredentialManagement();
        cm.setId(credentialId);
        cm.setUserId(userId);
        cm.setCredentialDecoded(jsonCredential);
        cm.setCredentialStatus(CREDENTIAL_ISSUED);
        cm.setCredentialFormat(JWT_VC);
        cm.setModifiedAt(new Timestamp(System.currentTimeMillis()));


        when(credentialManagementRepository.findByUserIdOrderByModifiedAtDesc(eq(userId), any()))
                .thenReturn(Flux.just(cm));
        when(objectMapper.readValue(eq(cm.getCredentialDecoded()), any(TypeReference.class)))
                .thenReturn(parsedCredential);

        StepVerifier.create(credentialManagementService.getCredentials(userId, 0, 10, "modifiedAt", Sort.Direction.DESC))
                .expectNextMatches(item -> item.credential().get("name").equals("John Doe"))
                .verifyComplete();

        verify(credentialManagementRepository).findByUserIdOrderByModifiedAtDesc(eq(userId), any());
    }

    @Test
    void getCredentialTest() throws JsonProcessingException {
        UUID credentialId = UUID.randomUUID();
        String userId = "user123";
        String jsonCredential = "{\"name\": \"John Doe\"}";
        Map<String, Object> parsedCredential = Map.of("name", "John Doe");

        CredentialManagement cm = new CredentialManagement();
        cm.setId(credentialId);
        cm.setUserId(userId);
        cm.setCredentialDecoded(jsonCredential);
        cm.setCredentialStatus(CREDENTIAL_ISSUED);
        cm.setCredentialFormat(JWT_VC);
        cm.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        when(credentialManagementRepository.findByIdAndUserId(credentialId, userId))
                .thenReturn(Mono.just(cm));
        when(objectMapper.readValue(eq(cm.getCredentialDecoded()), any(TypeReference.class)))
                .thenReturn(parsedCredential);


        StepVerifier.create(credentialManagementService.getCredential(credentialId, userId))
                .expectNextMatches(item -> item.credential().get("name").equals("John Doe"))
                .verifyComplete();

        verify(credentialManagementRepository).findByIdAndUserId(credentialId, userId);
    }
}

