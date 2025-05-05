package es.in2.issuer.backend.shared.domain.exception;

public class WellKnownInfoFetchException extends RuntimeException {
    public WellKnownInfoFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
