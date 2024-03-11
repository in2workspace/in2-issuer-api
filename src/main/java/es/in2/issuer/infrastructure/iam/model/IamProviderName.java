package es.in2.issuer.infrastructure.iam.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum IamProviderName {
    KEYCLOAK("keycloak"), OKTA("Okta");

    private final String providerName;

    @Override
    public String toString() {
        return providerName;
    }

}
