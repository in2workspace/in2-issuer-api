package es.in2.issuer.api.config.properties;

import es.in2.issuer.api.util.Utils;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

import java.util.Optional;

public record OpenApiInfoProperties(String title, String version, String description, String termsOfService,
                                    @NestedConfigurationProperty OpenApiInfoContactProperties contact,
                                    @NestedConfigurationProperty OpenApiInfoLicenseProperties license) {

    @ConstructorBinding
    public OpenApiInfoProperties(String title, String version, String description, String termsOfService,
                                 OpenApiInfoContactProperties contact, OpenApiInfoLicenseProperties license) {
        this.title = Utils.isNullOrBlank(title) ? "<name of your company>" : title;
        this.version = Utils.isNullOrBlank(version) ? "1.0.0-SNAPSHOT" : version;
        this.description = Utils.isNullOrBlank(description) ? "<description of your company>" : description;
        this.termsOfService = Utils.isNullOrBlank(termsOfService) ? "https://www.example.com/terms-of-service" : termsOfService;
        this.contact = Optional.ofNullable(contact).orElse(new OpenApiInfoContactProperties(null, null, null));
        this.license = Optional.ofNullable(license).orElse(new OpenApiInfoLicenseProperties(null, null));
    }

}