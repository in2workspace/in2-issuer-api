package es.in2.issuer.domain.service.impl;

import es.in2.issuer.domain.model.AppNonceValidationResponse;
import es.in2.issuer.domain.model.NonceResponse;
import es.in2.issuer.domain.service.NonceManagementService;
import es.in2.issuer.infrastructure.repository.CacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NonceManagementServiceImpl implements NonceManagementService {

    private final CacheStore<String> cacheStore;

    @Override
    public Mono<NonceResponse> saveAccessTokenAndNonce(AppNonceValidationResponse appNonceValidationResponse) {
        return generateNonce()
                .flatMap(nonce -> storeCredentialResponseInMemoryCache(nonce, appNonceValidationResponse.accessToken())
                        .thenReturn(NonceResponse.builder()
                                .nonce(nonce)
                                .nonceExpiresIn("600")
                                .build()));
    }

    private Mono<String> generateNonce() {
        return Mono.fromCallable(() -> Base64.getUrlEncoder().encodeToString(convertUUIDToBytes(UUID.randomUUID())));
    }

    private Mono<Void> storeCredentialResponseInMemoryCache(String nonce, String token) {
        return Mono.fromRunnable(() -> {
            log.info("***** Nonce code: " + nonce);
            cacheStore.add(nonce, token);
        });
    }

    private byte[] convertUUIDToBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }

}
