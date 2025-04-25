package es.in2.issuer.backend.shared.domain.exception;

import java.io.Serial;

public class ParseCredentialJsonException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public ParseCredentialJsonException(String message) {
        super(message);
    }
}
