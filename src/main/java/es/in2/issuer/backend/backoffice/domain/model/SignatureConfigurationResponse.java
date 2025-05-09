package es.in2.issuer.backend.backoffice.domain.model;


import es.in2.issuer.backend.shared.domain.model.enums.SignatureMode;
import lombok.Builder;

import java.util.UUID;

@Builder
public record SignatureConfigurationResponse(UUID id,
                                             String organizationIdentifier,
                                             boolean enableRemoteSignature,
                                             SignatureMode signatureMode,
                                             UUID cloudProviderId,
                                             String clientId,
                                             String credentialId,
                                             String credentialName
) {
}
