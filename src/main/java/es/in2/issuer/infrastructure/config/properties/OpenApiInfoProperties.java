package es.in2.issuer.infrastructure.config.properties;

import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

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

}
