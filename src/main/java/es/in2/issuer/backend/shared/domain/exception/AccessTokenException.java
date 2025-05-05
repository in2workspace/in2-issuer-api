package es.in2.issuer.backend.shared.domain.exception;

import java.io.Serial;

public class AccessTokenException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public AccessTokenException(String message) {
        super(message);
    }

    public AccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
