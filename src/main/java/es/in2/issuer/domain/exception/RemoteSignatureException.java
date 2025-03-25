package es.in2.issuer.domain.exception;

import java.io.Serial;

public class RemoteSignatureException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public RemoteSignatureException(String message) {
        super(message);
    }
    public RemoteSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
