package es.in2.issuer.backend.shared.domain.exception;

public class ParseErrorException extends RuntimeException {
    public ParseErrorException(String message) {
        super(message);
    }
}

