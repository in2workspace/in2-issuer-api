package es.in2.issuer.domain.model.dto;

import es.in2.issuer.domain.model.enums.SignatureMode;

import java.util.UUID;

public record SignatureConfigWithProviderName(UUID id,
                                              String organizationIdentifier,
                                              boolean enableRemoteSignature,
                                              SignatureMode signatureMode,
                                              String cloudProviderName,
                                              String clientId,
                                              String credentialId,
                                              String credentialName
                                             ) {

}
