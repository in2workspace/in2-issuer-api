package es.in2.issuer.shared.domain.exception;

public class InvalidCredentialFormatException extends RuntimeException {
    public InvalidCredentialFormatException(String message) {
        super(message);
    }
}
