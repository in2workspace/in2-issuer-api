package es.in2.issuer.api.config.properties;

import es.in2.issuer.api.util.Utils;
import org.springframework.boot.context.properties.bind.ConstructorBinding;

public record OpenApiInfoContactProperties(String email, String name, String url) {

    @ConstructorBinding
    public OpenApiInfoContactProperties(String email, String name, String url) {
        this.email = Utils.isNullOrBlank(email) ? "<email of your company>" : email;
        this.name = Utils.isNullOrBlank(name) ? "<name of your company>" : name;
        this.url = Utils.isNullOrBlank(url) ? "<url of your company>" : url;
    }

}
