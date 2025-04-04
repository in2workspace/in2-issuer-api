package es.in2.issuer.backend.domain.exception;

public class InsufficientPermissionException extends Exception {
    public InsufficientPermissionException(String message) {
        super(message);
    }
}
