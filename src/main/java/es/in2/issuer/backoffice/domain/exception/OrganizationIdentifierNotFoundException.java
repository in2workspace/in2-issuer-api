package es.in2.issuer.backoffice.domain.exception;

import java.io.Serial;

public class OrganizationIdentifierNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    public OrganizationIdentifierNotFoundException(String message) {
        super(message);
    }
}
