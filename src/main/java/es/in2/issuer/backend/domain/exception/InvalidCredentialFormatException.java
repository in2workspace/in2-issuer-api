package es.in2.issuer.backend.domain.exception;

public class InvalidCredentialFormatException extends RuntimeException {
    public InvalidCredentialFormatException(String message) {
        super(message);
    }
}
