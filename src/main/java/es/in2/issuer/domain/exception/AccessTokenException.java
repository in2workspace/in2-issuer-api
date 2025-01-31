package es.in2.issuer.domain.exception;

public class AccessTokenException extends Exception {
    public AccessTokenException(String message) {
        super(message);
    }

    public AccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
