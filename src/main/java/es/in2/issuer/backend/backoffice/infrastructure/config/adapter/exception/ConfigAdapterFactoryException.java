package es.in2.issuer.backend.backoffice.infrastructure.config.adapter.exception;

public class ConfigAdapterFactoryException extends RuntimeException {

    private static final String MESSAGE = "Error creating ConfigAdapterFactory. There should be only one ConfigAdapter. Found: ";

    public ConfigAdapterFactoryException(int size) {
        super(MESSAGE + size);
    }

}
