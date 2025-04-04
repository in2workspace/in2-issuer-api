package es.in2.issuer.backend.domain.exception;

public class TransactionCodeNotFoundException extends Exception{
    public TransactionCodeNotFoundException(String message) {
        super(message);
    }
}
