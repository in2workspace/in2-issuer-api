package es.in2.issuer.backend.backoffice.infrastructure.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

/**
 * Configuration intended to be used by the OpenAPI documentation.
 *
 * @param server - server information
 * @param info   - organization information
 */
@ConfigurationProperties(prefix = "openapi")
public record OpenApiProperties(@NestedConfigurationProperty OpenApiServerProperties server,
                                @NestedConfigurationProperty OpenApiInfoProperties info) {

    @ConstructorBinding
    public OpenApiProperties(OpenApiServerProperties server, OpenApiInfoProperties info) {
        this.server = Optional.ofNullable(server).orElse(new OpenApiServerProperties(null, null));
        this.info = Optional.ofNullable(info).orElse(new OpenApiInfoProperties(null, null, null, null, null, null));
    }

    public record OpenApiServerProperties(String url, String description) {
    }

    public record OpenApiInfoProperties(String title, String version, String description, String termsOfService,
                                        @NestedConfigurationProperty OpenApiInfoContactProperties contact,
                                        @NestedConfigurationProperty OpenApiInfoLicenseProperties license) {

        @ConstructorBinding
        public OpenApiInfoProperties(String title, String version, String description, String termsOfService,
                                     OpenApiInfoContactProperties contact, OpenApiInfoLicenseProperties license) {
            this.title = title;
            this.version = version;
            this.description = description;
            this.termsOfService = termsOfService;
            this.contact = Optional.ofNullable(contact).orElse(new OpenApiInfoContactProperties(null, null, null));
            this.license = Optional.ofNullable(license).orElse(new OpenApiInfoLicenseProperties(null, null));
        }

        public record OpenApiInfoContactProperties(String email, String name, String url) {
        }

        public record OpenApiInfoLicenseProperties(String name, String url) {
        }

    }


}
