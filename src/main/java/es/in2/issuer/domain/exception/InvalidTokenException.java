package es.in2.issuer.domain.exception;

public class InvalidTokenException extends Exception {

    private static final String DEFAULT_MESSAGE = "The request contains the wrong Access Token or the Access Token is missing";

    public InvalidTokenException() {
        super(DEFAULT_MESSAGE);
    }

}
