package es.in2.issuer.shared.domain.model.dto;

import lombok.Builder;

// todo: eliminar esta entidad
@Builder
public record CredentialOfferData(
        CustomCredentialOffer credentialOffer,
        String employeeEmail,
        String pin
) {
}
