package es.in2.issuer.domain.exception;
public class PreAuthorizationCodeGetException extends RuntimeException {
    public PreAuthorizationCodeGetException(String message) {
        super(message);
    }
}