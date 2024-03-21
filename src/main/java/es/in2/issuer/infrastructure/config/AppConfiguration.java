package es.in2.issuer.infrastructure.config;

import es.in2.issuer.infrastructure.config.properties.AppConfigurationProperties;
import es.in2.issuer.infrastructure.configuration.service.GenericConfigAdapter;
import es.in2.issuer.infrastructure.configuration.util.ConfigAdapterFactory;
import org.springframework.stereotype.Component;

@Component
public class AppConfiguration {

    private final GenericConfigAdapter genericConfigAdapter;
    private final AppConfigurationProperties appConfigurationProperties;

    public AppConfiguration(ConfigAdapterFactory configAdapterFactory, AppConfigurationProperties appConfigurationProperties){
        this.genericConfigAdapter = configAdapterFactory.getAdapter();
        this.appConfigurationProperties = appConfigurationProperties;
    }

    public String getIamExternalDomain() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.iamExternalDomain());
    }

    public String getIamInternalDomain() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.iamInternalDomain());
    }

    public String getIssuerDomain() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.issuerDomain());
    }

    public String getAuthenticSourcesDomain() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.authenticSourcesDomain());
    }

    public String getKeyVaultDomain() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.keyVaultDomain());
    }

    public String getRemoteSignatureDomain() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.remoteSignatureDomain());
    }

    public String getIssuerDid() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.issuerDid());
    }

    public String getJwtDecoderPath() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.jwtDecoderPath());
    }

    public String getJwtDecoderLocalPath() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.jwtDecoderLocalPath());
    }

    public String getPreAuthCodeUriTemplate() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.preAuthCodeUriTemplate());
    }
    public String getTokenUriTemplate() {
        return genericConfigAdapter.getConfiguration(appConfigurationProperties.tokenUriTemplate());
    }
}
