package es.in2.issuer.backend.shared.domain.model.dto;

import lombok.Builder;

@Builder
public record DeferredCredentialMetadataDeferredResponse(
        String id,
        String procedureId,
        String transactionId,
        String vc
) {
}
