package es.in2.issuer.domain.exception;

import java.io.Serial;

public class SignatureProcessingException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public SignatureProcessingException(String message) {
        super(message);
    }

    public SignatureProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
