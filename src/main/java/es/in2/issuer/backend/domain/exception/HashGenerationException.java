package es.in2.issuer.backend.domain.exception;

import java.io.Serial;

public class HashGenerationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public HashGenerationException(String message) {
        super(message);
    }

    public HashGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
