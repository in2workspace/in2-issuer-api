package es.in2.issuer.infrastructure.config.adapter.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AzureConfigurationSettingExceptionTest {

    @Test
    public void testConstructor() {
        String expectedMessage = "sample message";
        AzureConfigurationSettingException exception = new AzureConfigurationSettingException(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
    }
}