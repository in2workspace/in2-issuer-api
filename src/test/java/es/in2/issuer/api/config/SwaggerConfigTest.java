package es.in2.issuer.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class SwaggerConfigTest {

    private SwaggerConfig swaggerConfig;

    @BeforeEach
    public void setUp() {
        // Manually instantiate the SwaggerConfig
        swaggerConfig = new SwaggerConfig();
    }

    @Test
    public void testPublicApiGroupedOpenApiNotNull() {
        GroupedOpenApi publicApi = swaggerConfig.publicApi();
        assertNotNull(publicApi, "Public API GroupedOpenApi should not be null");
    }

    @Test
    public void testPrivateApiGroupedOpenApiNotNull() {
        GroupedOpenApi privateApi = swaggerConfig.privateApi();
        assertNotNull(privateApi, "Private API GroupedOpenApi should not be null");
    }
}