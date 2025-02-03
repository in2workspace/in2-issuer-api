package es.in2.issuer.domain.model.dto;

import lombok.Builder;

@Builder
public record CTransactionCodeDetails (
        String cTransactionCode,
        int cTransactionCodeExpiresIn
) {
}
