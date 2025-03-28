package es.in2.issuer.domain.exception;

import java.io.Serial;

public class CredentialOfferNotificationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    public CredentialOfferNotificationException(String message) {
        super(message);
    }
}
