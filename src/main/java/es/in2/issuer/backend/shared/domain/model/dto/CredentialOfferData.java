package es.in2.issuer.backend.shared.domain.model.dto;

import lombok.Builder;

@Builder
public record CredentialOfferData(
        CustomCredentialOffer credentialOffer,
        String employeeEmail,
        String pin
) {
}
