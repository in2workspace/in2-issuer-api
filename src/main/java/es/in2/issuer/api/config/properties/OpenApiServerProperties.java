package es.in2.issuer.api.config.properties;

import es.in2.issuer.api.util.Utils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

@Slf4j
public record OpenApiServerProperties(String url, String description) {

    @ConstructorBinding
    public OpenApiServerProperties(String url, String description) {
        this.url = Utils.isNullOrBlank(url) ? "http://localhost:8080" : url;
        this.description = Utils.isNullOrBlank(description) ? "<server description>" : description;
    }

}