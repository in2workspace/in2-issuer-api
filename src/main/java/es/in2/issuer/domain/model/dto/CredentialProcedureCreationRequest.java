package es.in2.issuer.domain.model.dto;

import es.in2.issuer.domain.model.enums.CredentialType;
import lombok.Builder;

@Builder
public record CredentialProcedureCreationRequest(
        String credentialId,
        String organizationIdentifier,
        String credentialDecoded,
        CredentialType credentialType,
        String subject
        )
{
}
