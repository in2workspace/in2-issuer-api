package es.in2.issuer.domain.exception;

public class SignatureProcessingException extends Exception {
    public SignatureProcessingException(String message) {
        super(message);
    }

    public SignatureProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
