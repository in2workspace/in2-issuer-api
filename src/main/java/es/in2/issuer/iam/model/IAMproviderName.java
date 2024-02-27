package es.in2.issuer.iam.model;

public enum IAMproviderName {
    KEYCLOAK("keycloak");

    private final String providerName;

    IAMproviderName(String providerName) {
        this.providerName = providerName;
    }

    @Override
    public String toString() {
        return providerName;
    }
}