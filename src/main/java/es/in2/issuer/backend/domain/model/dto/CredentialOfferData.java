package es.in2.issuer.backend.domain.model.dto;

import lombok.Builder;

@Builder
public record CredentialOfferData(
        CustomCredentialOffer credentialOffer,
        String employeeEmail,
        String pin
) {
}
