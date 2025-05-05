package es.in2.issuer.backend.backoffice.domain.exception;

public class UserDoesNotExistException extends Exception {

    public UserDoesNotExistException(String message) {
        super(message);
    }

}
