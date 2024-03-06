package es.in2.issuer.iam.adapter.keycloak;

import es.in2.issuer.api.config.AppConfiguration;
import es.in2.issuer.iam.model.IamProviderName;
import es.in2.issuer.iam.service.GenericIamAdapter;
import es.in2.issuer.iam.util.IamSourceName;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@IamSourceName(name = IamProviderName.KEYCLOAK)
public class KeycloakIamAdapter implements GenericIamAdapter {

    private final AppConfiguration appConfiguration;
    private String keycloakInternalDomain;
    private String keycloakExternalDomain;
    private String did;

    @PostConstruct
    private void initializeKeycloakIAMadapter() {
        keycloakInternalDomain = appConfiguration.getIamInternalDomain();
        keycloakExternalDomain = appConfiguration.getIamExternalDomain();
        did = appConfiguration.getIamDid();
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
