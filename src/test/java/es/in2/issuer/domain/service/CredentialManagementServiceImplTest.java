package es.in2.issuer.domain.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import es.in2.issuer.domain.entity.CredentialDeferred;
import es.in2.issuer.domain.entity.CredentialManagement;
import es.in2.issuer.domain.model.SignedCredentials;
import es.in2.issuer.domain.repository.CredentialDeferredRepository;
import es.in2.issuer.domain.repository.CredentialManagementRepository;
import es.in2.issuer.domain.service.impl.CredentialManagementServiceImpl;
import es.in2.issuer.domain.util.CredentialStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    @Mock
    private VerifiableCredentialService verifiableCredentialService;

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
        String testCredential = "some_encoded_credential_data";

        when(credentialManagementRepository.findByIdAndUserId(credentialId, userId)).thenReturn(Mono.just(credentialManagement));

        when(credentialManagementRepository.save(any(CredentialManagement.class))).thenReturn(Mono.just(credentialManagement));

        when(credentialDeferredRepository.findByCredentialId(credentialId)).thenReturn(Mono.just(new CredentialDeferred()));

        when(credentialDeferredRepository.save(any(CredentialDeferred.class))).thenReturn(Mono.just(new CredentialDeferred()));

        StepVerifier.create(credentialManagementService.updateCredential(testCredential, credentialId, userId))
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
        String testUserId = "user123";
        String jsonCredential = "{\"name\": \"John Doe\"}";
        Map<String, Object> parsedCredential = Map.of("name", "John Doe");

        CredentialManagement cm = new CredentialManagement();
        cm.setId(credentialId);
        cm.setUserId(testUserId);
        cm.setCredentialDecoded(jsonCredential);
        cm.setCredentialStatus(CredentialStatus.ISSUED.getName());
        cm.setCredentialFormat(JWT_VC);
        cm.setModifiedAt(new Timestamp(System.currentTimeMillis()));


        when(credentialManagementRepository.findByUserIdOrderByModifiedAtDesc(eq(testUserId), any()))
                .thenReturn(Flux.just(cm));
        when(objectMapper.readValue(eq(cm.getCredentialDecoded()), any(TypeReference.class)))
                .thenReturn(parsedCredential);

        StepVerifier.create(credentialManagementService.getCredentials(testUserId, 0, 10, "modifiedAt", Sort.Direction.DESC))
                .expectNextMatches(item -> item.credential().get("name").equals("John Doe"))
                .verifyComplete();

        verify(credentialManagementRepository).findByUserIdOrderByModifiedAtDesc(eq(testUserId), any());
    }

    @Test
    void getCredentialTest() throws JsonProcessingException {
        UUID credentialId = UUID.randomUUID();
        String credentialTestUserId = "user123";
        String jsonCredential = "{\"name\": \"John Doe\"}";
        Map<String, Object> parsedCredential = Map.of("name", "John Doe");

        CredentialManagement cm = new CredentialManagement();
        cm.setId(credentialId);
        cm.setUserId(credentialTestUserId);
        cm.setCredentialDecoded(jsonCredential);
        cm.setCredentialStatus(CredentialStatus.ISSUED.getName());
        cm.setCredentialFormat(JWT_VC);
        cm.setModifiedAt(new Timestamp(System.currentTimeMillis()));

        when(credentialManagementRepository.findByIdAndUserId(credentialId, credentialTestUserId))
                .thenReturn(Mono.just(cm));
        when(objectMapper.readValue(eq(cm.getCredentialDecoded()), any(TypeReference.class)))
                .thenReturn(parsedCredential);


        StepVerifier.create(credentialManagementService.getCredential(credentialId, credentialTestUserId))
                .expectNextMatches(item -> item.credential().get("name").equals("John Doe"))
                .verifyComplete();

        verify(credentialManagementRepository).findByIdAndUserId(credentialId, credentialTestUserId);
    }

    @Test
    void getPendingCredentialsTest() throws JsonProcessingException {
        UUID credentialId = UUID.randomUUID();
        String testUserId = "user123";
        String jsonCredential = "{\"name\": \"John Doe\"}";
        String jsonPayload = "{\"vc\": {\"name\": \"John Doe\"}}";
        Map<String, Object> parsedCredential = Map.of("name", "John Doe");

        CredentialManagement cm = new CredentialManagement();
        cm.setId(credentialId);
        cm.setUserId(testUserId);
        cm.setCredentialDecoded(jsonCredential);
        cm.setCredentialStatus(CredentialStatus.ISSUED.getName());
        cm.setCredentialFormat(JWT_VC);
        cm.setModifiedAt(new Timestamp(System.currentTimeMillis()));


        when(credentialManagementRepository.findByUserIdAndCredentialStatusOrderByModifiedAtDesc(eq(testUserId), eq(CredentialStatus.ISSUED.getName()), any(Pageable.class)))
                .thenReturn(Flux.just(cm));
        when(verifiableCredentialService.generateDeferredVcPayLoad(jsonCredential)).thenReturn(Mono.just(jsonPayload));
        when(objectMapper.readValue(eq(jsonPayload), any(TypeReference.class)))
                .thenReturn(parsedCredential);

        StepVerifier.create(credentialManagementService.getPendingCredentials(testUserId, 0, 10, "modifiedAt", Sort.Direction.DESC))
                .expectNextMatches(item -> item.credentials().get(0).credential().get("name").equals("John Doe"))
                .verifyComplete();

        verify(credentialManagementRepository).findByUserIdAndCredentialStatusOrderByModifiedAtDesc(eq(testUserId), eq(CredentialStatus.ISSUED.getName()), any(Pageable.class));
    }

    @Test
    void testUpdateCredentials() throws JsonProcessingException {
        UUID credentialId = credentialManagement.getId();
        String testCredential = "eyJhbGciOiJSUzI1NiIsImN0eSI6Impzb24iLCJraWQiOiJNRmd3VUtST01Fd3hDekFKQmdOVkJBWVRBa1ZUTVFzd0NRWURWUVFJREFKemRERUtNQWdHQTFVRUJ3d0JiREVLTUFnR0ExVUVDZ3dCYnpFTE1Ba0dBMVVFQ3d3Q2IzVXhDekFKQmdOVkJBTU1BbU51QWdSbFZma20iLCJ4NXQjUzI1NiI6IkdPd1ByMmNNQThlOHpZWEdsbVRTWG1fVnhlX2dJUTZ5anZzV2VmTi1qdkkiLCJ4NWMiOlsiTUlJREZEQ0NBZnlnQXdJQkFnSUVaVlg1SmpBTkJna3Foa2lHOXcwQkFRc0ZBREJNTVFzd0NRWURWUVFHRXdKRlV6RUxNQWtHQTFVRUNBd0NjM1F4Q2pBSUJnTlZCQWNNQVd3eENqQUlCZ05WQkFvTUFXOHhDekFKQmdOVkJBc01BbTkxTVFzd0NRWURWUVFEREFKamJqQWVGdzB5TXpFeE1UWXhNVEV5TXpoYUZ3MHlOREV4TVRVeE1URXlNemhhTUV3eEN6QUpCZ05WQkFZVEFrVlRNUXN3Q1FZRFZRUUlEQUp6ZERFS01BZ0dBMVVFQnd3QmJERUtNQWdHQTFVRUNnd0JiekVMTUFrR0ExVUVDd3dDYjNVeEN6QUpCZ05WQkFNTUFtTnVNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQXZmRFpxT3M1SnlHMzdRbmhPbUtaazlLN0lsZTBUMFdseWg5SXNISjBkejNuYk9YZmhGd0JOeXUvZEVYMzgyVWh1dzdab2tTS3BEaTUxSzY4TjduNlJNanpud3hvZUJVaGlSZDE4VVlwRlFjZE45cFNwMStOQjhVNUhlZkx6ZHVsaTgyVmRLU1dFTFJBT2NXazU5ZHJXNEFZYVdPNVZUVndXVmhlUUJxZW9xN1FBSWVXMkN3K0xCSWpnNjRETVBYVFc4UGxaaHRUOTlRZ3VtQTlRZlJJZ0lsQzV1Z0RKanE1OFVSbFBkVWFXR0sxQmEvcjJwanJKYkdRempMR2MraHFBOSsxdlplakN2ZzJ0QVlLMU1PRGMyMTlnM0IvRlgrZGtFUUl5TXpZaUhjQlF3WU1lY1dyUEpyWHpSYzdxZmhqcnFRMC8vU2hwUWx1SEN6eHkwYzdwUUlEQVFBQk1BMEdDU3FHU0liM0RRRUJDd1VBQTRJQkFRQ2lrdHZ1RDlNUFQxaXhUc05KcE84MHlNT3RYOHhFUzZpM2J3YU85VXlQdzFvclZObnJoVEVGVzJoSnJrNnFjTXBqVm92MmlvYVdMLzkzbFkxQ1hxMkFROU0rQzVlVVlMR2kwSVF4THIyRWZpNFVueVI1S3pvOEVyTFpzYjV2SjZlYWxEKy94eFk0dmJTeEIrM3U5N3hNYXR3QjlzYWc4WFgrZ1RiNE9nSVIrcENsU0VUN3haS3BjQkR0NjFJNXpTQldpT2hMQzYyd1B2RVhrQU4xaW5GYUZJZlptdkY4clF2M3VSNVcwZzhSUXFFbEdLWHBQV24rVDZDbGszTjhmbEdrdlQxZDlZWVVJdlB1RTZzaVAwY0IzS25PcTZLbWhpUGVnYzZZZ1R5UzhKdzJpWmlsMEVpTGRpZUlxT002SDRkQ0orSXFYQUkxb1RzS1JaRTFCUk5IIl0sInR5cCI6Impvc2UiLCJzaWdUIjoiMjAyNC0wNS0xN1QwODowMjozNVoiLCJjcml0IjpbInNpZ1QiXX0.ew0KICAgICAgICAgICAgICAgICJzdWIiOiAiZGlkOmtleTp6RG5hZVdUM2RWbzNjVjJ6cjdHWWdkRnhCYVB1ZFRqeGJMelFGREtEZmRZdnRBaFlpIiwNCiAgICAgICAgICAgICAgICAibmJmIjogMTcxNTkzMjg2NywNCiAgICAgICAgICAgICAgICAiaXNzIjogImRpZDprZXk6ejZNa3FtYUNUMkpxZFV0TGVLYWg3dEVWZk5YdERYdFF5ajR5eEVnVjExWTVDcVVhIiwNCiAgICAgICAgICAgICAgICAiZXhwIjogMTcxODUyNDg2NywNCiAgICAgICAgICAgICAgICAiaWF0IjogMTcxNTkzMjg2NywNCiAgICAgICAgICAgICAgICAianRpIjogIjI4ZjhkMjA4LTc2MGMtNDk2MC04YmFmLTJiMjJiNzQ3MWZiYiIsDQogICAgICAgICAgICAgICAgInZjIjogew0KICAgICAgICAgICAgICAgICAgICAiaWQiOiAiMjhmOGQyMDgtNzYwYy00OTYwLThiYWYtMmIyMmI3NDcxZmJiIiwNCiAgICAgICAgICAgICAgICAgICAgInR5cGUiOiBbDQogICAgICAgICAgICAgICAgICAgICAgICAiVmVyaWZpYWJsZUNyZWRlbnRpYWwiLA0KICAgICAgICAgICAgICAgICAgICAgICAgIkxFQVJDcmVkZW50aWFsRW1wbG95ZWUiDQogICAgICAgICAgICAgICAgICAgIF0sDQogICAgICAgICAgICAgICAgICAgICJjcmVkZW50aWFsU3ViamVjdCI6IHsNCiAgICAgICAgICAgICAgICAgICAgICAgICJtYW5kYXRlIjogew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICJpZCI6ICI0ZTNjMDJiOC01YzU3LTQ2NzktOGFhNS01MDJkNjI0ODRhZjUiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICJsaWZlX3NwYW4iOiB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJlbmRfZGF0ZV90aW1lIjogIjIwMjUtMDQtMDIgMDk6MjM6MjIuNjM3MzQ1MTIyICswMDAwIFVUQyIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJzdGFydF9kYXRlX3RpbWUiOiAiMjAyNC0wNC0wMiAwOToyMzoyMi42MzczNDUxMjIgKzAwMDAgVVRDIg0KICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgIm1hbmRhdGVlIjogew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiaWQiOiAiZGlkOmtleTp6RG5hZVdUM2RWbzNjVjJ6cjdHWWdkRnhCYVB1ZFRqeGJMelFGREtEZmRZdnRBaFlpIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImVtYWlsIjogIm9yaW9sLmNhbmFkZXNAaW4yLmVzIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImZpcnN0X25hbWUiOiAiT3Jpb2wiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAiZ2VuZGVyIjogIk0iLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAibGFzdF9uYW1lIjogIkNhbmFkw6lzIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIm1vYmlsZV9waG9uZSI6ICIrMzQ2NjYzMzY2OTkiDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgfSwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAibWFuZGF0b3IiOiB7DQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb21tb25OYW1lIjogIklOMiIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJjb3VudHJ5IjogIkVTIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImVtYWlsQWRkcmVzcyI6ICJycmhoQGluMi5lcyIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJvcmdhbml6YXRpb24iOiAiSU4yLCBJbmdlbmllcsOtYSBkZSBsYSBJbmZvcm1hY2nDs24sIFMuTC4iLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAib3JnYW5pemF0aW9uSWRlbnRpZmllciI6ICJWQVRFUy1CNjA2NDU5MDAiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAic2VyaWFsTnVtYmVyIjogIkI2MDY0NTkwMCINCiAgICAgICAgICAgICAgICAgICAgICAgICAgICB9LA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICJwb3dlciI6IFsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgew0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgImlkIjogIjZiOGYzMTM3LWE1N2EtNDZhNS05N2U3LTExMTdhMjAxNDJmYiIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidG1mX2FjdGlvbiI6ICJFeGVjdXRlIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ0bWZfZG9tYWluIjogIkRPTUUiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInRtZl9mdW5jdGlvbiI6ICJPbmJvYXJkaW5nIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ0bWZfdHlwZSI6ICJEb21haW4iDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0sDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIHsNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJpZCI6ICJhZDliMTUwOS02MGVhLTQ3ZDQtOTg3OC0xOGI1ODFkOGUxOWIiLA0KICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgInRtZl9hY3Rpb24iOiBbDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIkNyZWF0ZSIsDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIlVwZGF0ZSINCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIF0sDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAidG1mX2RvbWFpbiI6ICJET01FIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ0bWZfZnVuY3Rpb24iOiAiUHJvZHVjdE9mZmVyaW5nIiwNCiAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgICJ0bWZfdHlwZSI6ICJEb21haW4iDQogICAgICAgICAgICAgICAgICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgICAgICAgICAgICAgICBdDQogICAgICAgICAgICAgICAgICAgICAgICB9DQogICAgICAgICAgICAgICAgICAgIH0sDQogICAgICAgICAgICAgICAgICAgICJleHBpcmF0aW9uRGF0ZSI6ICIyMDI0LTA2LTE2VDA4OjAxOjA3LjYyNzIxOTk3M1oiLA0KICAgICAgICAgICAgICAgICAgICAiaXNzdWFuY2VEYXRlIjogIjIwMjQtMDUtMTdUMDg6MDE6MDcuNjI5Mzk3MTU2WiIsDQogICAgICAgICAgICAgICAgICAgICJpc3N1ZXIiOiAiZGlkOmtleTp6Nk1rcW1hQ1QySnFkVXRMZUthaDd0RVZmTlh0RFh0UXlqNHl4RWdWMTFZNUNxVWEiLA0KICAgICAgICAgICAgICAgICAgICAidmFsaWRGcm9tIjogIjIwMjQtMDUtMTdUMDg6MDE6MDcuNjI5Mzk3MTU2WiINCiAgICAgICAgICAgICAgICB9DQogICAgICAgICAgICB9.Xh9SpkWOJc_tOwHumS5BHOL2ergoacwmR0Ziw3A3mUaN2M-MyfqU7XiezAhulx392wJbaYp2LRxJWd9DmbUIS-25a9sxl9tfgRMsdm3sgg_ieHW5KWA5CAuOhAafZi1aTTNpstOflSxPY7H-q14jJVWQ50-n_SKbyGqvCFA4E3UJpvC2rNQQsg-8C36PLl5L6-RUAluK4PxcZtpnNdU5lCcH0_VjPd5FKZsQGJ3iJbtelvSgkWsJaPTKih19xMTwQJq9YamcMeJZjU3MxjzonxO2QSDwgi8lWiBNthYl8-_8RLSKGxtS_JMLZLtGspGg-TaxJv2zaRTuwOTf16jrJw";
        SignedCredentials signedCredentials = SignedCredentials.builder().credentials(List.of(SignedCredentials.SignedCredential.builder().credential(testCredential).build())).build();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonNode = mapper.createObjectNode();
        jsonNode.put("jti", "test");


        when(credentialManagementRepository.findByUserIdAndCredentialDecodedContains(userId, "test")).thenReturn(Mono.just(credentialManagement));

        when(objectMapper.readTree(any(String.class))).thenReturn(jsonNode);

        when(credentialManagementRepository.save(any(CredentialManagement.class))).thenReturn(Mono.just(credentialManagement));

        when(credentialDeferredRepository.findByCredentialId(credentialId)).thenReturn(Mono.just(new CredentialDeferred()));

        when(credentialDeferredRepository.save(any(CredentialDeferred.class))).thenReturn(Mono.just(new CredentialDeferred()));

        StepVerifier.create(credentialManagementService.updateCredentials(signedCredentials, userId))
                .verifyComplete();

        verify(credentialManagementRepository).findByUserIdAndCredentialDecodedContains(userId, "test");
        verify(credentialManagementRepository).save(any(CredentialManagement.class));
        verify(credentialDeferredRepository).findByCredentialId(credentialId);
        verify(credentialDeferredRepository).save(any(CredentialDeferred.class));
    }
}

