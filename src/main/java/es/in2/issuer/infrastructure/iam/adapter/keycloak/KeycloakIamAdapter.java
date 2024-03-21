package es.in2.issuer.infrastructure.iam.adapter.keycloak;

import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.iam.model.IamProviderName;
import es.in2.issuer.infrastructure.iam.service.GenericIamAdapter;
import es.in2.issuer.infrastructure.iam.util.IamSourceName;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@IamSourceName(name = IamProviderName.KEYCLOAK)
public class KeycloakIamAdapter implements GenericIamAdapter {

    private final AppConfiguration appConfiguration;
    private final String keycloakInternalDomain;
    private final String keycloakExternalDomain;
    private final String jwtDecoderPath;
    private final String jwtDecoderLocalPath;
    private final String preAuthCodeUriTemplate;
    private final String tokenUriTemplate;
    private final String did;

    public KeycloakIamAdapter(AppConfiguration appConfiguration) {
        this.appConfiguration = appConfiguration;
        this.keycloakInternalDomain = appConfiguration.getIamInternalDomain();
        this.keycloakExternalDomain = appConfiguration.getIamExternalDomain();
        this.jwtDecoderPath = appConfiguration.getJwtDecoderPath();
        this.jwtDecoderLocalPath = appConfiguration.getJwtDecoderLocalPath();
        this.preAuthCodeUriTemplate = appConfiguration.getPreAuthCodeUriTemplate();
        this.tokenUriTemplate = appConfiguration.getTokenUriTemplate();
        this.did = appConfiguration.getIssuerDid();
    }
    @Override
    public String getJwtDecoder() {
        return "https://" + keycloakExternalDomain + jwtDecoderPath;
    }

    @Override
    public String getJwtDecoderLocal() {
        return keycloakInternalDomain + jwtDecoderLocalPath;
    }

    @Override
    public String getPreAuthCodeUri() {
        return keycloakInternalDomain + resolveTemplate(preAuthCodeUriTemplate, Map.of("did", did));
    }

    @Override
    public String getTokenUri() {
        return keycloakInternalDomain + resolveTemplate(tokenUriTemplate, Map.of("did", did));
    }

    private String resolveTemplate(String template, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return template;
    }
}
