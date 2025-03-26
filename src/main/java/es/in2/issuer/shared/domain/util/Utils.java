package es.in2.issuer.shared.domain.util;

import com.nimbusds.jose.util.Base64URL;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.UUID;

public final class Utils {
    private Utils() {
    }

    public static Mono<String> generateCustomNonce() {
        return convertUUIDToBytes(UUID.randomUUID())
                .map(uuidBytes -> Base64URL.encode(uuidBytes).toString());
    }

    private static Mono<byte[]> convertUUIDToBytes(UUID uuid) {
        return Mono.fromSupplier(() -> {
            ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
            byteBuffer.putLong(uuid.getMostSignificantBits());
            byteBuffer.putLong(uuid.getLeastSignificantBits());
            return byteBuffer.array();
        });
    }
}
