package es.in2.issuer.infrastructure.config;

import es.in2.issuer.infrastructure.config.adapter.ConfigAdapter;
import es.in2.issuer.infrastructure.config.adapter.factory.ConfigAdapterFactory;
import es.in2.issuer.infrastructure.config.properties.AuthServerProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class AuthServerConfig {

    private final ConfigAdapter configAdapter;
    private final AuthServerProperties authServerProperties;

    public AuthServerConfig(ConfigAdapterFactory configAdapterFactory, AuthServerProperties authServerProperties) {
        this.configAdapter = configAdapterFactory.getAdapter();
        this.authServerProperties = authServerProperties;
    }

    public String getAuthServerExternalDomain() {
        return configAdapter.getConfiguration(authServerProperties.externalDomain());
    }

    public String getAuthServerInternalDomain() {
        return configAdapter.getConfiguration(authServerProperties.internalDomain());
    }

    public String getAuthServerRealm() {
        return configAdapter.getConfiguration(authServerProperties.realm());
    }

    public String getAuthServerIssuerDid() {
        return configAdapter.getConfiguration(authServerProperties.paths().issuerDid());
    }

    public String getAuthServerJwtDecoderPath() {
        return configAdapter.getConfiguration(authServerProperties.paths().jwtDecoderPath());
    }

    public String getAuthServerJwtDecoderLocalPath() {
        return configAdapter.getConfiguration(authServerProperties.paths().jwtDecoderLocalPath());
    }

    public String getAuthServerJwtValidatorPath() {
        return configAdapter.getConfiguration(authServerProperties.paths().jwtValidatorPath());
    }

    public String getAuthServerPreAuthorizedCodePath() {
        return configAdapter.getConfiguration(authServerProperties.paths().preAuthorizedCodePath());
    }

    public String getAuthServerTokenPath() {
        return configAdapter.getConfiguration(authServerProperties.paths().tokenPath());
    }

    public String getAuthServerNonceValidationPath() {
        return configAdapter.getConfiguration(authServerProperties.paths().nonceValidationPath());
    }

    public String getJwtDecoder() {
        return getAuthServerInternalDomain() + getAuthServerJwtDecoderPath();
    }

    public String getJwtDecoderLocal() {
        return getAuthServerInternalDomain() + getAuthServerJwtDecoderLocalPath();
    }

    public String getPreAuthCodeUri() {
        return getAuthServerInternalDomain() + resolveTemplate(getAuthServerPreAuthorizedCodePath(), Map.of("did", getAuthServerIssuerDid()));
    }

    public String getTokenUri() {
        return getAuthServerInternalDomain() + resolveTemplate(getAuthServerTokenPath(), Map.of("did", getAuthServerIssuerDid()));
    }

    public String getJwtValidator() {
        return getAuthServerExternalDomain() + getAuthServerJwtValidatorPath();
    }

    private String resolveTemplate(String template, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return template;
    }

}
