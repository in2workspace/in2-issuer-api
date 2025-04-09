package es.in2.issuer.domain.exception;

public class MissingIdTokenHeaderException extends RuntimeException {
    public MissingIdTokenHeaderException(String message) {
        super(message);
    }
}
