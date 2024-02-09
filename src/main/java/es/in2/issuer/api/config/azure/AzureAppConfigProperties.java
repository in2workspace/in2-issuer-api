package es.in2.issuer.api.config.azure;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Getter
@Profile("!local")
public class AzureAppConfigProperties {

    @Value("${azure.app.config.endpoint}")
    public String azureConfigEndpoint;

    @Value("${azure.app.config.label.global}")
    public String azureConfigLabel;

}
