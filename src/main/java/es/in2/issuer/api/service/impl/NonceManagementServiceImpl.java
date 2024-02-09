package es.in2.issuer.api.service.impl;

import es.in2.issuer.api.model.dto.AppNonceValidationResponseDTO;
import es.in2.issuer.api.model.dto.NonceResponseDTO;
import es.in2.issuer.api.repository.CacheStore;
import es.in2.issuer.api.service.NonceManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NonceManagementServiceImpl implements NonceManagementService {

    private final CacheStore<String> cacheStore;

    @Override
    public Mono<NonceResponseDTO> saveAccessTokenAndNonce(AppNonceValidationResponseDTO appNonceValidationResponseDTO) {
        return Mono.fromCallable(() ->
                new NonceResponseDTO(storeCredentialResponseInMemoryCache(appNonceValidationResponseDTO.getAccessToken()), "600")
        );    }

    private String storeCredentialResponseInMemoryCache(String token) {
        String nonce = generateNonce();
        log.info("***** Nonce code: " + nonce);
        cacheStore.add(nonce, token);
        return nonce;
    }

    private String generateNonce() {
        return Base64.getUrlEncoder().encodeToString(convertUUIDToBytes(UUID.randomUUID()));
    }

    private byte[] convertUUIDToBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }
}
