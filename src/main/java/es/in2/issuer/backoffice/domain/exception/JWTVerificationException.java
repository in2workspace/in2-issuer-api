package es.in2.issuer.backoffice.domain.exception;

public class JWTVerificationException extends RuntimeException {

    public JWTVerificationException(String message) {
        super(message);
    }

}