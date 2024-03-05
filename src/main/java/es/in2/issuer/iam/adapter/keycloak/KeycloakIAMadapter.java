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
    private String keycloakInternalDomain;
    private String keycloakExternalDomain;
    private String did;

    @PostConstruct
    private void initializeKeycloakIAMadapter() {
        keycloakInternalDomain = appConfiguration.getIAMinternalDomain();
        keycloakExternalDomain = appConfiguration.getIAMexternalDomain();
        did = appConfiguration.getIAMdid();
    }

    @Override
    public String getJwtDecoder() {
        return "https://" + keycloakExternalDomain + "/realms/EAAProvider/protocol/openid-connect/certs";
    }

    @Override
    public String getJwtDecoderLocal() {
        return keycloakInternalDomain + "/realms/EAAProvider";
    }

    @Override
    public String getPreAuthCodeUri() {
        return keycloakInternalDomain + "/realms/EAAProvider/verifiable-credential/" + did + "/credential-offer";
    }

    @Override
    public String getTokenUri() {
        return keycloakInternalDomain + "/realms/EAAProvider/verifiable-credential/" + did + "/token";
    }
}
