package es.in2.issuer.api.exceptions;

public class ExpiredPreAuthorizedCodeException extends Exception {

    public ExpiredPreAuthorizedCodeException(String message) {
        super(message);
    }
}
