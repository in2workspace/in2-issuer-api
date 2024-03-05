package es.in2.issuer.iam.adapter.other;

import es.in2.issuer.api.config.AppConfiguration;
import es.in2.issuer.iam.model.IAMproviderName;
import es.in2.issuer.iam.service.GenericIAMadapter;
import es.in2.issuer.iam.util.IAMsourceName;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@IAMsourceName(name = IAMproviderName.OTHER)
public class otherIAMadapter implements GenericIAMadapter {

    private final AppConfiguration appConfiguration;
    private String keycloakBaseUrl;
    private String did;

    @PostConstruct
    private void initializeKeycloakIAMadapter() {
        keycloakBaseUrl = appConfiguration.getIAMexternalDomain();
        did = appConfiguration.getIAMdid();
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