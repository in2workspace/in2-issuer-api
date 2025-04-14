package es.in2.issuer.backend.domain.exception;

public class UnsupportedDIDTypeException extends RuntimeException{

    public UnsupportedDIDTypeException(String message) {
        super(message);
    }

}
