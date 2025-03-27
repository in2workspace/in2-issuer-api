package es.in2.issuer.domain.model.enums;

public enum CredentialStatus {
    WITHDRAWN, //fixme: Deprecated. Old status. Necessary to maintain retro compatibility.
    DRAFT,
    PEND_DOWNLOAD,
    PEND_SIGNATURE,
    ISSUED,
    VALID,
    REVOKED,
    EXPIRED
}
