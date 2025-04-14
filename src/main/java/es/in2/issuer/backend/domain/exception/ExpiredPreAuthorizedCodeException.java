package es.in2.issuer.backend.domain.exception;

public class ExpiredPreAuthorizedCodeException extends Exception {

    public ExpiredPreAuthorizedCodeException(String message) {
        super(message);
    }

}
