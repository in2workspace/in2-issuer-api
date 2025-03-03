package es.in2.issuer.domain.model.enums;

public enum CredentialStatus {
    WITHDRAWN, //fixme: Deprecated. Old status. Necessary to maintain retro compatibility.
    DRAFT,
    ISSUED,
    PEND_DOWNLOAD,
    PEND_SIGNATURE,
    VALID,
    REVOKED,
    EXPIRED
}
