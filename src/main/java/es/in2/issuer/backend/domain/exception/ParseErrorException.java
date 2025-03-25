package es.in2.issuer.backend.domain.exception;

public class ParseErrorException extends RuntimeException {
    public ParseErrorException(String message) {
        super(message);
    }
}

