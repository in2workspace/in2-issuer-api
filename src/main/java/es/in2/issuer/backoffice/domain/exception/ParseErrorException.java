package es.in2.issuer.backoffice.domain.exception;

public class ParseErrorException extends RuntimeException {
    public ParseErrorException(String message) {
        super(message);
    }
}

