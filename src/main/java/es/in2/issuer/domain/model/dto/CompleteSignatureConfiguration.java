package es.in2.issuer.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.in2.issuer.domain.model.enums.SignatureMode;
import lombok.Builder;

import java.util.UUID;

@Builder
public record CompleteSignatureConfiguration(
        UUID id,
        String organizationIdentifier,
        Boolean enableRemoteSignature,
        SignatureMode signatureMode,
        UUID cloudProviderId,
        String clientId,
        String credentialId,
        String credentialName,
        String secretRelativePath,

        String clientSecret,
        String credentialPassword,
        String secret
){
}
