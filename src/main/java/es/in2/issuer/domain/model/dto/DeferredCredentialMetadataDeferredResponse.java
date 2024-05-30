package es.in2.issuer.domain.model.dto;

import lombok.Builder;

@Builder
public record DeferredCredentialMetadataDeferredResponse(
        String id,
        String transactionId,

        String vc
) {
}
