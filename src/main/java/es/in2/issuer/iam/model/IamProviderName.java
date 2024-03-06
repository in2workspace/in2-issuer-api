package es.in2.issuer.iam.model;

public enum IamProviderName {
    KEYCLOAK("keycloak"),
    OTHER("other");

    private final String providerName;

    IamProviderName(String providerName) {
        this.providerName = providerName;
    }

    @Override
    public String toString() {
        return providerName;
    }
}