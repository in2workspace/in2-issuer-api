package es.in2.issuer.api.exceptions;

public class UserDoesNotExistException extends Exception {

    public UserDoesNotExistException(String message) {
        super(message);
    }
}
