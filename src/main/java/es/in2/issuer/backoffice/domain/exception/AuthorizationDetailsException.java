package es.in2.issuer.backoffice.domain.exception;

import java.io.Serial;

public class AuthorizationDetailsException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public AuthorizationDetailsException(String message, Throwable cause) {
        super(message, cause);
    }
}
