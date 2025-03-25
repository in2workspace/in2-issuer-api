package es.in2.issuer.infrastructure.config.adapter.exception;

import es.in2.issuer.backend.infrastructure.config.adapter.exception.AzureConfigurationSettingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AzureConfigurationSettingExceptionTest {

    @Test
    void testConstructor() {
        String expectedMessage = "sample message";
        AzureConfigurationSettingException exception = new AzureConfigurationSettingException(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
    }
}