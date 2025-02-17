package es.in2.issuer.domain.exception;

public class RemoteSignatureException extends Exception {
    public RemoteSignatureException(String message) {
        super(message);
    }
    public RemoteSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}
