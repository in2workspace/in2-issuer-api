package es.in2.issuer.domain.exception;

import java.io.Serial;

public class ParseCredentialJsonException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    public ParseCredentialJsonException(String message) {
        super(message);
    }
}
