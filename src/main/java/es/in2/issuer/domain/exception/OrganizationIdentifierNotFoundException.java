package es.in2.issuer.domain.exception;

public class OrganizationIdentifierNotFoundException extends RuntimeException {
    public OrganizationIdentifierNotFoundException(String message) {
        super(message);
    }
}
