package es.in2.issuer.infrastructure.iam.exception;

public class IamAdapterFactoryException extends RuntimeException {

    private static final String MESSAGE = "Error creating IamAdapterFactory. There should be only one IamAdapter. Found: ";

    public IamAdapterFactoryException(int size) {
        super(MESSAGE + size);
    }

}
