package es.in2.issuer.backoffice.domain.exception;

public class JWTParsingException extends RuntimeException{

    public JWTParsingException(String message) {
        super(message);
    }

}