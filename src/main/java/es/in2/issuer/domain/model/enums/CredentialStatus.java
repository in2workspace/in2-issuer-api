package es.in2.issuer.domain.model.enums;

public enum CredentialStatus {
    WITHDRAWN,
    DRAFT, // Old status. Necessary to maintain retro compatibility
    ISSUED,
    PEND_DOWNLOAD,
    VALID,
    REVOKED,
    EXPIRED
}
