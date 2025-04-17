package es.in2.issuer.backend.domain.exception;

import java.io.Serial;

public class EmailCommunicationException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;
    public EmailCommunicationException(String message) {
        super(message);
    }
}
