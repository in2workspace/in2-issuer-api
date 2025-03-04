package es.in2.issuer.domain.exception;

public class HashGenerationException extends Exception {
    public HashGenerationException(String message) {
        super(message);
    }

    public HashGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
