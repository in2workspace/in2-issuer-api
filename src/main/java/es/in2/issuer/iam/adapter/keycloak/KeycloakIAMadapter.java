package es.in2.issuer.iam.adapter.keycloak;

import es.in2.issuer.iam.model.IAMproviderName;
import es.in2.issuer.iam.service.GenericIAMadapter;
import es.in2.issuer.iam.util.IAMsourceName;
import org.springframework.stereotype.Component;

@Component
@IAMsourceName(name = IAMproviderName.KEYCLOAK)
public class KeycloakIAMadapter implements GenericIAMadapter {

    @Override
    public String getToken() {
        return null;
    }
}
