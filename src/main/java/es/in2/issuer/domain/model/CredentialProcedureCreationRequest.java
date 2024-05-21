package es.in2.issuer.domain.model;

import lombok.Builder;

@Builder
public record CredentialProcedureCreationRequest(
        String credentialId,
        String organizationIdentifier,
        String credentialDecoded
        )
{
}
