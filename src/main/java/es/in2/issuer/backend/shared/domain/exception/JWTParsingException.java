package es.in2.issuer.backend.shared.domain.exception;

public class JWTParsingException extends RuntimeException{

    public JWTParsingException(String message) {
        super(message);
    }

}