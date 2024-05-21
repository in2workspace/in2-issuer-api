package es.in2.issuer.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.in2.issuer.domain.entity.CredentialProcedure;
import es.in2.issuer.domain.entity.DeferredCredentialMetadata;
import es.in2.issuer.domain.repository.DeferredCredentialMetadataRepository;
import es.in2.issuer.domain.repository.CredentialProcedureRepository;
import es.in2.issuer.domain.service.impl.CredentialManagementServiceImpl;
import es.in2.issuer.domain.util.CredentialStatus;
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

import static es.in2.issuer.domain.util.Constants.JWT_VC;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeferredCredentialMetadataServiceImplTest {

    @Mock
    private CredentialProcedureRepository credentialProcedureRepository;

    @Mock
    private DeferredCredentialMetadataRepository deferredCredentialMetadataRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private VerifiableCredentialService verifiableCredentialService;

    @InjectMocks
    private CredentialManagementServiceImpl credentialManagementService;

    private final String userId = "user-id";
    private final String credential = "{\"example\": \"data\"}";
    private final String format = "json";
    private DeferredCredentialMetadata deferredCredentialMetadata;
    private CredentialProcedure credentialProcedure;

    @BeforeEach
    void setUp() {
        deferredCredentialMetadata = new DeferredCredentialMetadata();
        deferredCredentialMetadata.setId(UUID.randomUUID());
        deferredCredentialMetadata.setUserId(userId);
        deferredCredentialMetadata.setCredentialDecoded(credential);
        deferredCredentialMetadata.setCredentialFormat(format);
        deferredCredentialMetadata.setCredentialStatus("ISSUED");
        deferredCredentialMetadata.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        credentialProcedure = new CredentialProcedure();
        credentialProcedure.setId(UUID.randomUUID());
        credentialProcedure.setCredentialId(deferredCredentialMetadata.getId());
        credentialProcedure.setTransactionId("transaction-id");
    }

    @Test
    void testCommitCredential() {
        when(credentialProcedureRepository.save(any(DeferredCredentialMetadata.class))).thenReturn(Mono.just(deferredCredentialMetadata));
        when(deferredCredentialMetadataRepository.save(any(CredentialProcedure.class))).thenReturn(Mono.just(credentialProcedure));

        StepVerifier.create(credentialManagementService.commitCredential(credential, userId, format))
                .expectNextMatches(transactionId -> !transactionId.isEmpty())
                .verifyComplete();

        verify(credentialProcedureRepository).save(any(DeferredCredentialMetadata.class));
        verify(deferredCredentialMetadataRepository).save(any(CredentialProcedure.class));
    }

    @Test
    void testUpdateCredential() {
        UUID credentialId = deferredCredentialMetadata.getId();
        String credential = "some_encoded_credential_data";

        when(credentialProcedureRepository.findByIdAndUserId(credentialId, userId)).thenReturn(Mono.just(deferredCredentialMetadata));

        when(credentialProcedureRepository.save(any(DeferredCredentialMetadata.class))).thenReturn(Mono.just(deferredCredentialMetadata));

        when(deferredCredentialMetadataRepository.findByCredentialId(credentialId)).thenReturn(Mono.just(new CredentialProcedure()));

        when(deferredCredentialMetadataRepository.save(any(CredentialProcedure.class))).thenReturn(Mono.just(new CredentialProcedure()));

        StepVerifier.create(credentialManagementService.updateCredential(testCredential, credentialId, userId))
                .verifyComplete();

        verify(credentialProcedureRepository).findByIdAndUserId(credentialId, userId);
        verify(credentialProcedureRepository).save(any(DeferredCredentialMetadata.class));
        verify(deferredCredentialMetadataRepository).findByCredentialId(credentialId);
        verify(deferredCredentialMetadataRepository).save(any(CredentialProcedure.class));
    }

    @Test
    void testUpdateTransactionId() {
        when(deferredCredentialMetadataRepository.findByTransactionId("transaction-id")).thenReturn(Mono.just(credentialProcedure));
        when(deferredCredentialMetadataRepository.save(any(CredentialProcedure.class))).thenReturn(Mono.just(credentialProcedure));

        StepVerifier.create(credentialManagementService.updateTransactionId("transaction-id"))
                .expectNextMatches(newTransactionId -> !newTransactionId.isEmpty())
                .verifyComplete();

        verify(deferredCredentialMetadataRepository).findByTransactionId("transaction-id");
        verify(deferredCredentialMetadataRepository).save(any(CredentialProcedure.class));
    }

    @Test
    void testDeleteCredentialDeferred() {
        when(deferredCredentialMetadataRepository.findByTransactionId("transaction-id")).thenReturn(Mono.just(credentialProcedure));
        when(deferredCredentialMetadataRepository.delete(credentialProcedure)).thenReturn(Mono.empty());

        StepVerifier.create(credentialManagementService.deleteCredentialDeferred("transaction-id"))
                .verifyComplete();

        verify(deferredCredentialMetadataRepository).findByTransactionId("transaction-id");
        verify(deferredCredentialMetadataRepository).delete(credentialProcedure);
    }

    @Test
    void getCredentialsTest() throws JsonProcessingException {
        UUID credentialId = UUID.randomUUID();
        String testUserId = "user123";
        String jsonCredential = "{\"name\": \"John Doe\"}";
        Map<String, Object> parsedCredential = Map.of("name", "John Doe");

        DeferredCredentialMetadata cm = new DeferredCredentialMetadata();
        cm.setId(credentialId);
        cm.setUserId(testUserId);
        cm.setCredentialDecoded(jsonCredential);
        cm.setCredentialStatus(CredentialStatus.ISSUED.getName());
        cm.setCredentialFormat(JWT_VC);
        cm.setModifiedAt(new Timestamp(System.currentTimeMillis()));


        when(credentialProcedureRepository.findByUserIdOrderByModifiedAtDesc(eq(userId), any()))
                .thenReturn(Flux.just(cm));
        when(objectMapper.readValue(eq(cm.getCredentialDecoded()), any(TypeReference.class)))
                .thenReturn(parsedCredential);

        StepVerifier.create(credentialManagementService.getCredentials(testUserId, 0, 10, "modifiedAt", Sort.Direction.DESC))
                .expectNextMatches(item -> item.credential().get("name").equals("John Doe"))
                .verifyComplete();

        verify(credentialProcedureRepository).findByUserIdOrderByModifiedAtDesc(eq(userId), any());
    }

    @Test
    void getCredentialTest() throws JsonProcessingException {
        UUID credentialId = UUID.randomUUID();
        String credentialTestUserId = "user123";
        String jsonCredential = "{\"name\": \"John Doe\"}";
        Map<String, Object> parsedCredential = Map.of("name", "John Doe");

        DeferredCredentialMetadata cm = new DeferredCredentialMetadata();
        cm.setId(credentialId);
        cm.setUserId(credentialTestUserId);
        cm.setCredentialDecoded(jsonCredential);
        cm.setCredentialStatus(CredentialStatus.ISSUED.getName());
        cm.setCredentialFormat(JWT_VC);
        cm.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        when(credentialProcedureRepository.findByIdAndUserId(credentialId, userId))
                .thenReturn(Mono.just(cm));
        when(objectMapper.readValue(eq(cm.getCredentialDecoded()), any(TypeReference.class)))
                .thenReturn(parsedCredential);


        StepVerifier.create(credentialManagementService.getCredential(credentialId, credentialTestUserId))
                .expectNextMatches(item -> item.credential().get("name").equals("John Doe"))
                .verifyComplete();

        verify(credentialProcedureRepository).findByIdAndUserId(credentialId, userId);
    }
}

