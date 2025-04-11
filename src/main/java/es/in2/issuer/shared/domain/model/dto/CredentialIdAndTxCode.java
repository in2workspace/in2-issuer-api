package es.in2.issuer.shared.domain.model.dto;

import java.util.UUID;

public record CredentialIdAndTxCode(UUID credentialId, String TxCode) {
}
