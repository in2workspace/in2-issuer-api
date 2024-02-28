package es.in2.issuer.iam.adapter.keycloak;

import es.in2.issuer.api.config.AppConfiguration;
import es.in2.issuer.iam.model.IAMproviderName;
import es.in2.issuer.iam.service.GenericIAMadapter;
import es.in2.issuer.iam.util.IAMsourceName;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@IAMsourceName(name = IAMproviderName.KEYCLOAK)
public class KeycloakIAMadapter implements GenericIAMadapter {

    private final AppConfiguration appConfiguration;
    private String keycloakBaseUrl;
    private String did;

    @PostConstruct
    private void initializeKeycloakIAMadapter() {
        keycloakBaseUrl = appConfiguration.getIAMdomain();
        did = appConfiguration.getIAMdid();
    }

    @Override
    public String getJwtDecoder() {
        return "https://" + keycloakBaseUrl + "/realms/EAAProvider/protocol/openid-connect/certs";
    }

    @Override
    public String getJwtDecoderLocal() {
        return keycloakBaseUrl + "/realms/EAAProvider";
    }

    @Override
    public String getPreAuthCodeUri() {
        return keycloakBaseUrl + "/realms/EAAProvider/verifiable-credential/" + did + "/credential-offer";
    }

    @Override
    public String getTokenUri() {
        return keycloakBaseUrl + "/realms/EAAProvider/verifiable-credential/" + did + "/token";
    }
}
