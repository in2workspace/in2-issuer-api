package es.in2.issuer.backoffice.domain.exception;

public class CredentialAlreadyIssuedException extends RuntimeException {
    public CredentialAlreadyIssuedException(String message) {
        super(message);
    }
}
