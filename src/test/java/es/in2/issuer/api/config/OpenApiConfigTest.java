package es.in2.issuer.api.config;

import es.in2.issuer.api.config.properties.OpenApiProperties;
import es.in2.issuer.api.config.properties.OpenApiInfoProperties;
import es.in2.issuer.api.config.properties.OpenApiServerProperties;
import es.in2.issuer.api.util.HttpUtils;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import es.in2.issuer.api.config.properties.OpenApiInfoContactProperties;
import es.in2.issuer.api.config.properties.OpenApiInfoLicenseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class OpenApiConfigTest {

    @Mock
    private OpenApiProperties openApiProperties;

    @Mock
    private AppConfiguration appConfiguration;

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        openApiConfig = new OpenApiConfig(openApiProperties, appConfiguration);
    }

    @Test
    void testOpenApiConfiguration() {
        // Mock properties
        OpenApiInfoContactProperties contactProperties = new OpenApiInfoContactProperties("test@example.com","John Doe", "http://example.com");
        OpenApiInfoLicenseProperties licenseProperties = new OpenApiInfoLicenseProperties("Apache 2.0", "https://www.apache.org/licenses/LICENSE-2.0");
        OpenApiInfoProperties infoProperties = new OpenApiInfoProperties("Test API", "1.0", "Test Description", "http://example.com/tos", contactProperties, licenseProperties);
        when(openApiProperties.info()).thenReturn(infoProperties);
        OpenApiServerProperties serverProperties = new OpenApiServerProperties("Test Server","Test Description");
        when(openApiProperties.server()).thenReturn(serverProperties);

        // Mock app configuration
        when(appConfiguration.getIssuerDomain()).thenReturn("example.com");

        // Invoke method to test
        OpenAPI openAPI = openApiConfig.openApi();

        // Assertions
        assertNotNull(openAPI);
        Info info = openAPI.getInfo();
        assertNotNull(info);
        Contact contact = info.getContact();
        assertNotNull(contact);
        License license = info.getLicense();
        assertNotNull(license);
        assertEquals("Test API", info.getTitle());
        assertEquals("1.0", info.getVersion());
        assertEquals("John Doe", contact.getName());
        assertEquals("test@example.com", contact.getEmail());
        assertEquals("http://example.com", contact.getUrl());
        assertEquals("Apache 2.0", license.getName());
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0", license.getUrl());
        assertEquals(1, openAPI.getServers().size());
        Server server = openAPI.getServers().get(0);
        assertNotNull(server);
        assertEquals("Test Description", server.getDescription());
    }
}