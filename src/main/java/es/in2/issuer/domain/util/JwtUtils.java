package es.in2.issuer.domain.util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
@Slf4j
public class JwtUtils {
    public String getPayload(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("invalid JWT");
        }
        return parts[1];
    }

    public String decodePayload(String jwt) {
        String[] parts = jwt.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("invalid JWT");
        }

        byte[] decodedBytes = Base64.getDecoder().decode(parts[1]);
        return new String(decodedBytes);
    }
}