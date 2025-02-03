package es.in2.issuer.domain.exception;

public class AuthorizationDetailsException extends RuntimeException {
    public AuthorizationDetailsException(String message, Throwable cause) {
        super(message, cause);
    }
}
