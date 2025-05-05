package es.in2.issuer.backend.shared.domain.exception;

public class CredentialAlreadyIssuedException extends RuntimeException {
    public CredentialAlreadyIssuedException(String message) {
        super(message);
    }
}
