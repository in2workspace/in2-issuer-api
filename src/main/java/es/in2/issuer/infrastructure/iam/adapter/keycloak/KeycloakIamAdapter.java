package es.in2.issuer.infrastructure.iam.adapter.keycloak;

import es.in2.issuer.infrastructure.config.AppConfiguration;
import es.in2.issuer.infrastructure.iam.model.IamProviderName;
import es.in2.issuer.infrastructure.iam.service.GenericIamAdapter;
import es.in2.issuer.infrastructure.iam.util.IamSourceName;
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

    // todo: ¿Por qué necesitamos un PostConstruct?
    @PostConstruct
    private void initializeKeycloakIamAdapter() {
        keycloakInternalDomain = appConfiguration.getIamInternalDomain();
        keycloakExternalDomain = appConfiguration.getIamExternalDomain();
        // fixme: el did no es del IAM sino del Credential Issuer y debería ser obtenido de la configuración del Credential Issuer.
        //  Debe hacer match con el did:elsi del certificado eIDAS del módulo DSS
        did = appConfiguration.getIamDid();
    }

    @Override
    public String getJwtDecoder() {
        // fixme: los paths deberían ser obtenidos de la configuración
        return "https://" + keycloakExternalDomain + "/realms/EAAProvider/protocol/openid-connect/certs";
    }

    @Override
    public String getJwtDecoderLocal() {
        // fixme: los paths deberían ser obtenidos de la configuración
        return keycloakInternalDomain + "/realms/EAAProvider";
    }

    @Override
    public String getPreAuthCodeUri() {
        // fixme: los paths deberían ser obtenidos de la configuración
        return keycloakInternalDomain + "/realms/EAAProvider/verifiable-credential/" + did + "/credential-offer";
    }

    @Override
    public String getTokenUri() {
        // fixme: los paths deberían ser obtenidos de la configuración
        return keycloakInternalDomain + "/realms/EAAProvider/verifiable-credential/" + did + "/token";
    }

}
