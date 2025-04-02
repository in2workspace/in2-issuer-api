package es.in2.issuer.authserver.domain.service;

import es.in2.issuer.authserver.domain.service.impl.PreAuthorizedCodeCacheStoreImpl;
import es.in2.issuer.shared.domain.model.dto.CredentialIdAndTxCode;
import es.in2.issuer.shared.infrastructure.repository.CacheStoreRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreAuthorizedCodeCacheStoreRepositoryImplTest {

    @Mock
    CacheStoreRepository<CredentialIdAndTxCode> cacheStoreRepository;

    @InjectMocks
    PreAuthorizedCodeCacheStoreImpl preAuthorizedCodeCacheStore;

    @Test
    void itShouldSave() {
        String expectedPreAuthorizedCode = "1234";
        String txCode = "5678";

        UUID credentialId = UUID.fromString("762a8cf7-a872-41fc-8674-80243da68251");
        when(cacheStoreRepository.add(expectedPreAuthorizedCode,
                new CredentialIdAndTxCode(credentialId, txCode)))
                .thenReturn(Mono.just(expectedPreAuthorizedCode));

        var resultMono = preAuthorizedCodeCacheStore.save(
                "",
                expectedPreAuthorizedCode,
                credentialId,
                txCode);

        StepVerifier
                .create(resultMono)
                .assertNext(result -> assertThat(result).isEqualTo(expectedPreAuthorizedCode))
                .verifyComplete();
    }
}