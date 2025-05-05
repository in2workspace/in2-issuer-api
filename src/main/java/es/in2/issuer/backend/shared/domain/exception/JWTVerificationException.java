package es.in2.issuer.backend.shared.domain.exception;

public class JWTVerificationException extends RuntimeException {

    public JWTVerificationException(String message) {
        super(message);
    }

}