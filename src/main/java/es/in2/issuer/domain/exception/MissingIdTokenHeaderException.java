package es.in2.issuer.domain.exception;


import java.io.Serial;

public class MissingIdTokenHeaderException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    public MissingIdTokenHeaderException(String message) {
        super(message);
    }
}
