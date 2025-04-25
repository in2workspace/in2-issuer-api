package es.in2.issuer.backend.backoffice.domain.exception;

public class ExpiredPreAuthorizedCodeException extends Exception {

    public ExpiredPreAuthorizedCodeException(String message) {
        super(message);
    }

}
