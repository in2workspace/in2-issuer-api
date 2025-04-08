package es.in2.issuer.shared.domain.exception;

public class NonceValidationException extends Exception {
    public NonceValidationException(String message) {
        super(message);
    }
}
