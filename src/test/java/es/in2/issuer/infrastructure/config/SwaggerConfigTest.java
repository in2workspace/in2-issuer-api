package es.in2.issuer.infrastructure.config;

import es.in2.issuer.infrastructure.config.SwaggerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class SwaggerConfigTest {

    private SwaggerConfig swaggerConfig;

    @BeforeEach
    public void setUp() {
        // Manually instantiate the SwaggerConfig
        swaggerConfig = new SwaggerConfig();
    }

    @Test
    void testPublicApiGroupedOpenApiNotNull() {
        GroupedOpenApi publicApi = swaggerConfig.publicApi();
        assertNotNull(publicApi, "Public API GroupedOpenApi should not be null");
    }

    @Test
    void testPrivateApiGroupedOpenApiNotNull() {
        GroupedOpenApi privateApi = swaggerConfig.privateApi();
        assertNotNull(privateApi, "Private API GroupedOpenApi should not be null");
    }
}
