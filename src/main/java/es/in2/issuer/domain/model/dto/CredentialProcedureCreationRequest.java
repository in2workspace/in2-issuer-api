package es.in2.issuer.domain.model.dto;

import lombok.Builder;

@Builder
public record CredentialProcedureCreationRequest(
        String credentialId,
        String organizationIdentifier,
        String credentialDecoded
        )
{
}
