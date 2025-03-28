package es.in2.issuer.domain.exception;

import java.io.Serial;

public class CredentialOfferEmailException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    public CredentialOfferEmailException(String message) {
        super(message);
    }
}
