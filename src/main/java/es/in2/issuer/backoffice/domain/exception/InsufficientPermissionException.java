package es.in2.issuer.backoffice.domain.exception;

public class InsufficientPermissionException extends Exception {
    public InsufficientPermissionException(String message) {
        super(message);
    }
}
