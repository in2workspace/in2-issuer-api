package es.in2.issuer.backend.backoffice.domain.model;

import lombok.Builder;

@Builder
public record SignatureVaultSecret(

        String clientSecret,
        String credentialPassword,
        String secret
) { }