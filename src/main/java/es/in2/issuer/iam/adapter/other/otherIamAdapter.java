package es.in2.issuer.iam.adapter.other;

import es.in2.issuer.api.config.AppConfiguration;
import es.in2.issuer.iam.model.IamProviderName;
import es.in2.issuer.iam.service.GenericIamAdapter;
import es.in2.issuer.iam.util.IamSourceName;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@IamSourceName(name = IamProviderName.OTHER)
public class otherIamAdapter implements GenericIamAdapter {

    private final AppConfiguration appConfiguration;
    private String keycloakBaseUrl;
    private String did;

    @PostConstruct
    private void initializeKeycloakIAMadapter() {
        keycloakBaseUrl = appConfiguration.getIamExternalDomain();
        did = appConfiguration.getIamDid();
    }

    @Override
    public String getJwtDecoder() {
        return "test";
    }

    @Override
    public String getJwtDecoderLocal() {
        return "test";
    }

    @Override
    public String getPreAuthCodeUri() {
        return "test";
    }

    @Override
    public String getTokenUri() {
        return "test";
    }
}