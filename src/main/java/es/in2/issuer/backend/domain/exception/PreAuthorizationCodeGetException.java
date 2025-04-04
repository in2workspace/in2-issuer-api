package es.in2.issuer.backend.domain.exception;
public class PreAuthorizationCodeGetException extends RuntimeException {
    public PreAuthorizationCodeGetException(String message) {
        super(message);
    }
}