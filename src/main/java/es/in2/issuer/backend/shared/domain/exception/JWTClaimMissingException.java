package es.in2.issuer.backend.shared.domain.exception;

public class JWTClaimMissingException extends RuntimeException{

    public JWTClaimMissingException(String message) {
        super(message);
    }

}
