package es.in2.issuer.backend.shared.domain.exception;
public class PreAuthorizationCodeGetException extends RuntimeException {
    public PreAuthorizationCodeGetException(String message) {
        super(message);
    }
}