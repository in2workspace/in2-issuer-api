package es.in2.issuer.api.config.properties;

import es.in2.issuer.api.util.Utils;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

public record OpenApiInfoLicenseProperties(String name, String url) {

    @ConstructorBinding
    public OpenApiInfoLicenseProperties(String name, String url) {
        this.name = Utils.isNullOrBlank(name) ? "Apache 2.0" : name;
        this.url = Utils.isNullOrBlank(url) ? "https://www.apache.org/licenses/LICENSE-2.0.html" : url;
    }

}
