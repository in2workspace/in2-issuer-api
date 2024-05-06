package es.in2.issuer.domain.util;

import lombok.Getter;

@Getter
public enum CredentialStatus {
    ISSUED("issued"),
    VALID("valid");

    private final String status;

    CredentialStatus(String status) {
        this.status = status;
    }

}
