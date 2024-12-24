package es.in2.issuer.domain.exception;

public class CredentialAlreadyIssuedException extends RuntimeException {
    public CredentialAlreadyIssuedException(String message) {
        super(message);
    }
}
