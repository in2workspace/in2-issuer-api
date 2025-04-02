package es.in2.issuer.backend.domain.exception;

public class NonceValidationException extends Exception {
    public NonceValidationException(String message) {
        super(message);
    }
}
