package es.in2.issuer.backend.backoffice.domain.exception;

public class PublicKeyDecodingException extends RuntimeException {
    public PublicKeyDecodingException(String message) {
        super(message);
    }

    public PublicKeyDecodingException(String message, Throwable cause) {
        super(message, cause);
    }
}
