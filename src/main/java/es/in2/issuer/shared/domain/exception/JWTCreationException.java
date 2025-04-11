package es.in2.issuer.shared.domain.exception;

public class JWTCreationException extends RuntimeException {

    public JWTCreationException(String message) {
        super(message);
    }

}
