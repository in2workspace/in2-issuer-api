package es.in2.issuer.shared.domain.exception;

public class InsufficientPermissionException extends Exception {
    public InsufficientPermissionException(String message) {
        super(message);
    }
}
