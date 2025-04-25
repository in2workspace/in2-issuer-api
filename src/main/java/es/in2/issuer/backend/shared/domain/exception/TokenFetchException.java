package es.in2.issuer.backend.shared.domain.exception;

public class TokenFetchException extends RuntimeException {
    public TokenFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}