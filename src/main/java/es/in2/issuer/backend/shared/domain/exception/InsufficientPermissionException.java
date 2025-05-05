package es.in2.issuer.backend.shared.domain.exception;

public class InsufficientPermissionException extends Exception {
    public InsufficientPermissionException(String message) {
        super(message);
    }
}
