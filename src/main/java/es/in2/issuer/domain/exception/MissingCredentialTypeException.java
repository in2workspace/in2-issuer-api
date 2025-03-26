package es.in2.issuer.domain.exception;

import java.io.Serial;

public class MissingCredentialTypeException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public MissingCredentialTypeException(String message) {
        super(message);
    }
}
