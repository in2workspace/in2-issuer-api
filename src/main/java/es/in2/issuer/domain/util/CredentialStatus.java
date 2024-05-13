package es.in2.issuer.domain.util;

import lombok.Getter;

@Getter
public enum CredentialStatus {
    ISSUED("issued"),
    VALID("valid"),
    REVOKED("revoked"),
    EXPIRED("expired");

    private final String name;

    CredentialStatus(String name) {
        this.name = name;
    }

}
